package com.dddheroes.cinema.modules.seatsblocking.write.unblockseats

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatEvent
import com.dddheroes.cinema.modules.seatsblocking.events.SeatNotBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatNotUnblocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatUnblocked
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.sdk.application.CommandHandlerResult
import com.dddheroes.sdk.application.resultOf
import com.dddheroes.sdk.application.toCommandResult
import com.dddheroes.sdk.restapi.toResponseEntity
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.commandhandling.annotation.Command
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.eventhandling.gateway.EventAppender
import org.axonframework.messaging.eventstreaming.EventCriteria
import org.axonframework.messaging.eventstreaming.Tag
import org.axonframework.modelling.annotation.InjectEntity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant
import java.util.concurrent.CompletableFuture

////////////////////////////////////////////
////////// Domain
///////////////////////////////////////////

@Command(namespace = "SeatsBlocking", name = "UnblockSeats", version = "1.0.0")
data class UnblockSeats(
    val screeningId: ScreeningId,
    val seats: Set<SeatNumber>,
    val blockadeOwner: String,
    val issuedAt: Instant
) {
    val consistencyBoundaryId = ConsistencyBoundaryId(screeningId, seats)
}

data class ConsistencyBoundaryId(
    val screeningId: ScreeningId,
    val seats: Set<SeatNumber>
)

private data class State(
    val blockadeBySeat: Map<SeatNumber, String> = emptyMap()
)

private fun decide(command: UnblockSeats, state: State): List<SeatEvent> {
    val seatsBlockedByOthers = command.seats.filter { seat ->
        val blockedBy = state.blockadeBySeat[seat]
        blockedBy != null && blockedBy != command.blockadeOwner
    }

    if (seatsBlockedByOthers.isNotEmpty()) {
        throw IllegalStateException("Cannot unblock seats - some seats are blocked by others: $seatsBlockedByOthers")
    }

    return command.seats.mapNotNull { seat ->
        when (state.blockadeBySeat[seat]) {
            command.blockadeOwner -> SeatUnblocked(command.screeningId, seat, command.blockadeOwner, command.issuedAt)
            null -> null // Not blocked, idempotent
            else -> null
        }
    }
}

private fun evolve(state: State, event: SeatEvent): State = when (event) {
    is SeatBlocked -> state.copy(blockadeBySeat = state.blockadeBySeat + (event.seat to event.blockadeOwner))
    is SeatUnblocked -> state.copy(blockadeBySeat = state.blockadeBySeat - event.seat)
    is SeatNotBlocked -> state
    is SeatNotUnblocked -> state
}

////////////////////////////////////////////
////////// Application
///////////////////////////////////////////

@ConditionalOnProperty(name = ["slices.seatsblocking.write.unblockseats.enabled"])
@EventSourced(idType = ConsistencyBoundaryId::class)
private class UnblockSeatsEventSourcedState private constructor(val state: State) {

    @EntityCreator
    constructor() : this(State())

    @EventSourcingHandler
    fun evolve(event: SeatBlocked) = UnblockSeatsEventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: SeatUnblocked) = UnblockSeatsEventSourcedState(evolve(state, event))

    companion object {
        @JvmStatic
        @EventCriteriaBuilder
        fun resolveCriteria(consistencyBoundary: ConsistencyBoundaryId): EventCriteria {
            val screeningId = consistencyBoundary.screeningId.raw
            return EventCriteria.either(
                consistencyBoundary.seats.map {
                    EventCriteria.havingTags(
                            Tag.of(CinemaTags.SCREENING_ID, screeningId),
                            Tag.of(CinemaTags.SEAT_ID, it.toString())
                        )
                        .andBeingOneOfTypes(
                            "SeatsBlocking.SeatBlocked",
                            "SeatsBlocking.SeatUnblocked",
                        )
                }
            )
        }
    }
}

@ConditionalOnProperty(name = ["slices.seatsblocking.write.unblockseats.enabled"])
@Component
private class UnblockSeatsCommandHandler {

    @CommandHandler
    fun handle(
        command: UnblockSeats,
        @InjectEntity(idProperty = "consistencyBoundaryId") state: UnblockSeatsEventSourcedState,
        eventAppender: EventAppender
    ): CommandHandlerResult = resultOf {
        val events = decide(command, state.state)
        eventAppender.append(events)
        return events.toCommandResult()
    }

}

////////////////////////////////////////////
////////// Presentation
///////////////////////////////////////////

@ConditionalOnProperty(name = ["slices.seatsblocking.write.unblockseats.enabled"])
@RestController
@RequestMapping("screenings/{screeningId}")
private class UnblockSeatsRestApi(
    private val commandGateway: CommandGateway,
    private val clock: Clock
) {

    @JvmRecord
    data class Body(val seats: List<String>, val blockadeOwner: String)

    @DeleteMapping("/seats-blockades")
    fun unblockSeats(
        @PathVariable screeningId: String,
        @RequestBody body: Body
    ): CompletableFuture<ResponseEntity<Any>> {
        val command = UnblockSeats(
            screeningId = ScreeningId.of(screeningId),
            seats = body.seats.map { SeatNumber.from(it) }.toSet(),
            blockadeOwner = body.blockadeOwner,
            issuedAt = Instant.now(clock)
        )
        return commandGateway.send(command)
            .resultAs(CommandHandlerResult::class.java)
            .toResponseEntity()
    }
}
