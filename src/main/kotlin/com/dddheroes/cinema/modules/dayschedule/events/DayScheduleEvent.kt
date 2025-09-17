package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.shared.events.CinemaEvent
import org.axonframework.eventsourcing.annotation.EventTag

sealed interface DayScheduleEvent : CinemaEvent {
    @get:EventTag(key = CinemaTags.DAY_SCHEDULE_ID)
    val dayScheduleId: DayScheduleId
}