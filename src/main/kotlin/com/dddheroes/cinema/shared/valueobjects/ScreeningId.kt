package com.dddheroes.cinema.shared.valueobjects

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class ScreeningId(val raw: String) {
    override fun toString(): String = raw

    companion object {
        fun of(raw: String): ScreeningId = ScreeningId(raw)
        fun random(): ScreeningId = ScreeningId(UUID.randomUUID().toString())
    }
}
