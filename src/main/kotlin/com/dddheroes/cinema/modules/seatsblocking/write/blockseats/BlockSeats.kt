package com.dddheroes.cinema.modules.seatsblocking.write.blockseats

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.events.ScreeningScheduled
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatEvent
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
import com.dddheroes.cinema.modules.seatsblocking.events.SeatUnblocked
import com.dddheroes.cinema.shared.events.CinemaEvent
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.sdk.application.anyOf
import org.axonframework.commandhandling.annotation.CommandHandler
import org.axonframework.eventhandling.gateway.EventAppender
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.AnnotationBasedEventCriteriaResolverDefinition
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder
import org.axonframework.eventsourcing.annotation.EventSourcedEntity
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule
import org.axonframework.eventstreaming.EventCriteria
import org.axonframework.eventstreaming.Tag
import org.axonframework.messaging.QualifiedName
import org.axonframework.modelling.annotation.InjectEntity
import org.axonframework.modelling.configuration.StatefulCommandHandlingModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.ZoneOffset

////////////////////////////////////////////
////////// Domain
///////////////////////////////////////////

data class ConsistencyBoundaryId(
    val screeningId: ScreeningId,
    val seats: Set<SeatNumber>
)

data class BlockSeats(
    val screeningId: ScreeningId,
    val seats: Set<SeatNumber>,
    val blockadeOwner: String,
    val issuedAt: Instant
) {
    val consistencyBoundaryId = ConsistencyBoundaryId(screeningId, seats)
}

internal data class State(
    val blockadeBySeat: Map<SeatNumber, String?> = emptyMap(),
    val screeningEndTime: Instant? = null
)

private fun decide(command: BlockSeats, state: State): List<SeatEvent> {
    if (state.screeningEndTime == null) {
        throw IllegalStateException("Cannot block seats - screening not scheduled yet")
    }
    if (command.issuedAt.isAfter(state.screeningEndTime)) {
        throw IllegalStateException("Cannot block seats - screening has already ended")
    }
    // Check if seats are placed (exist in state map)
    val seatsNotPlaced = command.seats.filter { seat ->
        !state.blockadeBySeat.containsKey(seat)
    }

    if (seatsNotPlaced.isNotEmpty()) {
        throw kotlin.IllegalStateException("Cannot block seats - must be placed first")
    }

    val seatsBlockedByOthers = command.seats.filter { seat ->
        val blockedBy = state.blockadeBySeat[seat]
        blockedBy != null && blockedBy != command.blockadeOwner
    }

    if (seatsBlockedByOthers.isNotEmpty()) {
        throw kotlin.IllegalStateException("Cannot block seats - some seats are already blocked by others: $seatsBlockedByOthers")
    }

    return command.seats.mapNotNull { seat ->
        val blockedBy = state.blockadeBySeat[seat]
        when (blockedBy) {
            null -> SeatBlocked(command.screeningId, seat, command.blockadeOwner, command.issuedAt)
            command.blockadeOwner -> null // Already blocked by same owner, no event needed
            else -> null
        }
    }
}

private fun evolve(state: State, event: CinemaEvent): State = when (event) {
    is SeatPlaced -> state.copy(blockadeBySeat = state.blockadeBySeat + (event.seat to null))
    is SeatBlocked -> state.copy(blockadeBySeat = state.blockadeBySeat + (event.seat to event.blockadeOwner))
    is SeatUnblocked -> state.copy(blockadeBySeat = state.blockadeBySeat + (event.seat to null))
    is ScreeningScheduled -> state.copy(
        screeningEndTime = event.dayScheduleId.raw.atTime(event.endTime).toInstant(ZoneOffset.UTC)
    )

    else -> state
}

////////////////////////////////////////////
////////// Application
///////////////////////////////////////////

@EventSourcedEntity(criteriaResolverDefinition = AnnotationBasedEventCriteriaResolverDefinition::class) // @ConsistencyBoundary
internal class EventSourcedState private constructor(val state: State) {

    @EntityCreator
    constructor() : this(State())

    @EventSourcingHandler
    fun evolve(event: SeatPlaced) = EventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: SeatBlocked) = EventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: SeatUnblocked) = EventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: ScreeningScheduled) = EventSourcedState(evolve(state, event))

    companion object {
        @JvmStatic
        @EventCriteriaBuilder // @ConsistencyBoundary(Definition/Query/Condition/Lock)
        fun resolveCriteria(consistencyBoundary: ConsistencyBoundaryId): EventCriteria {
            val seats = anyOf(
                consistencyBoundary.seats.map {
                    EventCriteria.havingTags(Tag.of(CinemaTags.SEAT_ID, it.toString()))
                        .andBeingOneOfTypes(
                            QualifiedName(SeatPlaced::class.java),
                            QualifiedName(SeatBlocked::class.java),
                            QualifiedName(SeatUnblocked::class.java),
                        )
                }
            )
            val screeningSchedules =
                EventCriteria.havingTags(Tag.of(CinemaTags.SCREENING_ID, consistencyBoundary.screeningId.raw))
                    .andBeingOneOfTypes(QualifiedName(ScreeningScheduled::class.java))

            return EventCriteria.either(seats, screeningSchedules)
        }
    }
}

private class BlockSeatsCommandHandler {

    @CommandHandler
    fun handle(
        command: BlockSeats,
        @InjectEntity(idProperty = "consistencyBoundaryId") state: EventSourcedState,
        eventAppender: EventAppender
    ) {
        val events = decide(command, state.state)
        eventAppender.append(events)
    }

}

@Configuration
internal class BlockSeatsWriteSliceConfig {

    @Bean
    fun blockSeatsSlice(): StatefulCommandHandlingModule =
        StatefulCommandHandlingModule.named(BlockSeats::class.simpleName!!)
            .entities()
            .entity(
                EventSourcedEntityModule.annotated(
                    ConsistencyBoundaryId::class.java,
                    EventSourcedState::class.java
                )
            )
            .commandHandlers()
            .annotatedCommandHandlingComponent { BlockSeatsCommandHandler() }
            .build()
}