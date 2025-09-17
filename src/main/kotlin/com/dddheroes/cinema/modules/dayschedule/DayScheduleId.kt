package com.dddheroes.cinema.modules.dayschedule

import java.time.LocalDate

@JvmInline
value class DayScheduleId(val raw: LocalDate) {
    override fun toString(): String = raw.toString()
    fun toLocalDate(): LocalDate = raw

    companion object {
        fun of(raw: LocalDate): DayScheduleId = DayScheduleId(raw)
        fun of(raw: String): DayScheduleId = DayScheduleId(LocalDate.parse(raw))
    }
}
