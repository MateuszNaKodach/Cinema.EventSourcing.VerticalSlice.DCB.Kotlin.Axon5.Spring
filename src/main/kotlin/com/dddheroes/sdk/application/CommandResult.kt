package com.dddheroes.sdk.application

import com.dddheroes.sdk.domain.DomainEvent
import com.dddheroes.sdk.domain.DomainRuleViolatedException
import com.dddheroes.sdk.domain.FailureEvent

sealed class CommandResult {
    data object Success : CommandResult()
    data class Failure(val message: String) : CommandResult()

    fun throwIfFailure(): CommandResult {
        if (this is Failure) {
            throw DomainRuleViolatedException(message)
        }
        return this;
    }
}

inline fun <T, R> T.resultOf(block: T.() -> R): CommandResult {
    return try {
        block()
        CommandResult.Success
    } catch (e: Throwable) {
        CommandResult.Failure(e.message ?: "Unknown error")
    }
}

fun <T : DomainEvent> Collection<T>.toCommandResult(): CommandResult {
    val failureEvents = this.filterIsInstance<FailureEvent>()
    return if (failureEvents.isEmpty()) {
        CommandResult.Success
    } else {
        val messages = failureEvents.joinToString(", ") { it.reason }
        CommandResult.Failure(messages)
    }
}