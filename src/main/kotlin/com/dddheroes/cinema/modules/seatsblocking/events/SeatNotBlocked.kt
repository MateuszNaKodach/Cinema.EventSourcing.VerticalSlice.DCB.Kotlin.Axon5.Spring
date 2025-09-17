package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.sdk.domain.FailureEvent
import java.time.Instant

data class SeatNotBlocked(
    override val screeningId: ScreeningId,
    override val seat: SeatNumber,
    override val reason: String,
    val triedBy: String,
    override val occurredAt: Instant
) : SeatEvent, FailureEvent