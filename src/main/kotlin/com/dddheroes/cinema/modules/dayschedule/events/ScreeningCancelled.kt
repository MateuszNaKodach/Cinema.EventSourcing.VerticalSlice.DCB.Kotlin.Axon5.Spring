package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant

@Event(namespace = "DaySchedule", name = "ScreeningCancelled", version = "1.0.0")
data class ScreeningCancelled(
    override val dayScheduleId: DayScheduleId,
    @get:EventTag(key = CinemaTags.SCREENING_ID)
    val screeningId: ScreeningId,
    override val occurredAt: Instant
) : DayScheduleEvent