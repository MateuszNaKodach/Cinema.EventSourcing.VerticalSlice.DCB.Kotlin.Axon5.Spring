package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant

@Event(namespace = "SeatsBlocking", name = "SeatBlocked", version = "1.0.0")
data class SeatBlocked(
    @EventTag(key = "screeningId")
    val screeningId: ScreeningId,
    @EventTag(key = "seatId")
    val seat: SeatNumber,
    val blockadeOwner: String,
) : SeatEvent