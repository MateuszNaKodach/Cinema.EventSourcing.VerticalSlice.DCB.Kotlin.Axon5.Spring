package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant

@Event(namespace = "SeatsBlocking", name = "SeatPlaced", version = "1.0.0")
data class SeatPlaced(
    override val screeningId: ScreeningId,
    override val seat: SeatNumber,
    override val occurredAt: Instant
) : SeatEvent