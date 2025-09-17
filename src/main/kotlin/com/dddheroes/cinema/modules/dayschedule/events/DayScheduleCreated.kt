package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import java.time.Instant
import java.time.LocalTime

data class DayScheduleCreated(
    override val dayScheduleId: DayScheduleId,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    override val occurredAt: Instant
) : DayScheduleEvent