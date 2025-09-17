package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.events.CinemaEvent
import org.axonframework.eventsourcing.annotation.EventTag

sealed interface SeatEvent : CinemaEvent {
    @get:EventTag(key = CinemaTags.SCREENING_ID)
    val screeningId: ScreeningId
    @get:EventTag(key = CinemaTags.SEAT_ID)
    val seat: SeatNumber
}