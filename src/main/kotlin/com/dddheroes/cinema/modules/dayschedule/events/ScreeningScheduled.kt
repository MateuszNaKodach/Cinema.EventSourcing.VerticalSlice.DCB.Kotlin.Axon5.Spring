package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.CinemaTags
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import org.axonframework.eventsourcing.annotation.EventTag
import java.time.Instant
import java.time.LocalTime

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

// if we want: cannot reserve seat after screening finished - we can couple togherther in one transaction.
// no-one can edit screening like reserving seat, cognitive load increase - someome who program blocking seat / reservations must know that
// if we agree for eventual consistency it's easier to keep them separate


// todo: sprawdzic slownik:
// The correct English translation for "seans filmowy" is typically "film screening" or simply "screening". While "screening" is sometimes used informally, "screening" is the more accurate and commonly used term in contexts related to movies.
//
//So, for a movie event, "film screening" is preferred over "film screening" or "screening"