package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import java.time.Instant

data class SeatPlaced(
    override val screeningId: ScreeningId,
    override val seat: SeatNumber,
    override val occurredAt: Instant
) : SeatEvent