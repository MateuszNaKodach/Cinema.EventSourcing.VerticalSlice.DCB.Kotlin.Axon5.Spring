package com.dddheroes.cinema.modules.seatsblocking.write.blockseat

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatEvent
import com.dddheroes.cinema.modules.seatsblocking.events.SeatNotBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatNotUnblocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant
import java.util.concurrent.CompletableFuture

////////////////////////////////////////////
////////// Domain
///////////////////////////////////////////

data class SeatId(
    val screeningId: ScreeningId,
    val seat: SeatNumber
)

@Command(namespace = "SeatsBlocking", name = "BlockSeat", version = "1.0.0")
data class BlockSeat(
    val screeningId: ScreeningId,
    val seat: SeatNumber,
    val blockadeOwner: String,
    val issuedAt: Instant
) {
    val seatId = SeatId(screeningId, seat)
}

private data class State(val placed: Boolean = false, val blockedBy: String? = null)

private val initialState = State()

private fun decide(command: BlockSeat, state: State): List<SeatEvent> {
    if (!state.placed) {
        return listOf(
            SeatNotBlocked(
                command.screeningId,
                command.seat,
                "Seat must be placed before it can be blocked",
                command.blockadeOwner,
                command.issuedAt
            )
        )
    }
    return when (state.blockedBy) {
        null -> listOf(
            SeatBlocked(
                command.screeningId,
                command.seat,
                command.blockadeOwner,
                command.issuedAt
            )
        )

        command.blockadeOwner -> emptyList()
        else -> listOf(
            SeatNotBlocked(
                command.screeningId,
                command.seat,
                "Seat is already blocked by ${state.blockedBy}",
                command.blockadeOwner,
                command.issuedAt
            )
        )
    }
}

private fun evolve(state: State, event: SeatEvent): State = when (event) {
    is SeatPlaced -> state.copy(placed = true)
    is SeatBlocked -> state.copy(blockedBy = event.blockadeOwner)
    is SeatUnblocked -> state.copy(blockedBy = null)
    is SeatNotBlocked -> state
    is SeatNotUnblocked -> state
}

////////////////////////////////////////////
////////// Application
///////////////////////////////////////////

@ConditionalOnProperty(name = ["slices.seatsblocking.write.blockseat.enabled"])
@EventSourced(idType = SeatId::class)
private class EventSourcedState private constructor(val state: State) {

    @EntityCreator
    constructor() : this(initialState)

    @EventSourcingHandler
    fun evolve(event: SeatPlaced) = EventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: SeatBlocked) = EventSourcedState(evolve(state, event))

    @EventSourcingHandler
    fun evolve(event: SeatUnblocked) = EventSourcedState(evolve(state, event))

    companion object {
        @JvmStatic
        @EventCriteriaBuilder
        fun resolveCriteria(seatId: SeatId): EventCriteria {
            return EventCriteria.havingTags(
                Tag.of(CinemaTags.SCREENING_ID, seatId.screeningId.raw),
                Tag.of(CinemaTags.SEAT_ID, seatId.seat.toString())
            ).andBeingOneOfTypes(
                "SeatsBlocking.SeatPlaced",
                "SeatsBlocking.SeatBlocked",
                "SeatsBlocking.SeatUnblocked",
            )
        }
    }
}

@ConditionalOnProperty(name = ["slices.seatsblocking.write.blockseat.enabled"])
@Component
private class BlockSeatCommandHandler {

    @CommandHandler
    fun handle(
        command: BlockSeat,
        @InjectEntity(idProperty = "seatId") state: EventSourcedState,
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

@ConditionalOnProperty(name = ["slices.seatsblocking.write.blockseat.enabled"])
@RestController
@RequestMapping("screenings/{screeningId}")
private class BlockSeatRestApi(
    private val commandGateway: CommandGateway,
    private val clock: Clock
) {

    @JvmRecord
    data class Body(val blockadeOwner: String)

    @PutMapping("/seats-blockades/{seat}")
    fun blockSeat(
        @PathVariable screeningId: String,
        @PathVariable seat: String,
        @RequestBody body: Body
    ): CompletableFuture<ResponseEntity<Any>> {
        val command = BlockSeat(
            screeningId = ScreeningId.of(screeningId),
            seat = SeatNumber.from(seat),
            blockadeOwner = body.blockadeOwner,
            issuedAt = Instant.now(clock)
        )
        return commandGateway.send(command)
            .resultAs(CommandHandlerResult::class.java)
            .toResponseEntity()
    }
}
