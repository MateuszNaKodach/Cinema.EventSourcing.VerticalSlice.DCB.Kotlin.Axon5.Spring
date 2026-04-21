package com.dddheroes.sdk.domain

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}