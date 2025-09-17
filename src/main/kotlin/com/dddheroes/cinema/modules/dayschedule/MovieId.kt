package com.dddheroes.cinema.modules.dayschedule

import java.util.UUID

@JvmInline
value class MovieId(val raw: String) {
    override fun toString(): String = raw

    companion object {
        fun random(): MovieId = MovieId(UUID.randomUUID().toString())
    }
}
