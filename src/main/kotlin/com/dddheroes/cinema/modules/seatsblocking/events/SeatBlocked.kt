package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant

@Event(namespace = "SeatsBlocking", name = "SeatBlocked", version = "1.0.0")
data class SeatBlocked(
    override val screeningId: ScreeningId,
    override val seat: SeatNumber,
    val blockadeOwner: String,
    override val occurredAt: Instant
) : SeatEvent