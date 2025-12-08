package com.dddheroes.cinema.modules.seatsblocking.events

import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SeatBlocked(
    override val screeningId: ScreeningId,
    override val seat: SeatNumber,
    val blockadeOwner: String,
    @Contextual override val occurredAt: Instant
) : SeatEvent