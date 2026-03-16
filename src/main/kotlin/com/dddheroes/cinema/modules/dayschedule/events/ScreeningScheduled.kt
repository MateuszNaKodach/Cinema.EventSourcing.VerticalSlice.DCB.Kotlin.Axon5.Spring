package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant
import java.time.LocalTime

@Event(namespace = "DaySchedule", name = "ScreeningScheduled", version = "1.0.0")
data class ScreeningScheduled(
    override val dayScheduleId: DayScheduleId,
    @get:EventTag(key = CinemaTags.SCREENING_ID)
    val screeningId: ScreeningId,
    @get:EventTag(key = CinemaTags.MOVIE_ID)
    val movieId: MovieId,
    val startTime: LocalTime,
    val endTime: LocalTime,
    override val occurredAt: Instant
) : DayScheduleEvent