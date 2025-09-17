package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.eventsourcing.annotation.EventTag
import java.time.Instant

data class ScreeningCancelled(
    override val dayScheduleId: DayScheduleId,
    @get:EventTag(key = CinemaTags.SCREENING_ID)
    val screeningId: ScreeningId,
    override val occurredAt: Instant
) : DayScheduleEvent