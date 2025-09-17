package com.dddheroes.cinema.shared.valueobjects

import java.util.UUID

@JvmInline
value class PaymentId(val raw: String) {
    override fun toString(): String = raw

    companion object {
        fun random(): PaymentId = PaymentId(UUID.randomUUID().toString())
    }
}