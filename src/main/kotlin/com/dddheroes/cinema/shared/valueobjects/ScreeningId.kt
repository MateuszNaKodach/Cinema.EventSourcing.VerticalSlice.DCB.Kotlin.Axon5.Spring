package com.dddheroes.cinema.shared.valueobjects

import java.util.UUID

@JvmInline
value class ScreeningId(val raw: String) {
    override fun toString(): String = raw

    companion object {
        fun of(raw: String): ScreeningId = ScreeningId(raw)
        fun random(): ScreeningId = ScreeningId(UUID.randomUUID().toString())
    }
}
