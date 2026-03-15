package com.dddheroes.cinema.modules.dayschedule.events

import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import org.axonframework.messaging.eventhandling.annotation.Event
import java.time.Instant
import java.time.LocalTime

@Event(namespace = "DaySchedule", name = "DayScheduleCreated", version = "1.0.0")
data class DayScheduleCreated(
    override val dayScheduleId: DayScheduleId,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    override val occurredAt: Instant
) : DayScheduleEvent