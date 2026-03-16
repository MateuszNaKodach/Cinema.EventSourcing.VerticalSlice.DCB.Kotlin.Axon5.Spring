package com.dddheroes.cinema

import com.dddheroes.sdk.domain.DomainRuleViolatedException
import com.dddheroes.sdk.restapi.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    @ExceptionHandler(DomainRuleViolatedException::class)
    fun handleDomainRuleViolatedException(e: DomainRuleViolatedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest()
            .body(ErrorResponse(e.message ?: "Unknown error occurred"))
    }
}
