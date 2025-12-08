package com.dddheroes.cinema.modules.admin.processors

import com.dddheroes.cinema.CustomResetContext
import kotlinx.serialization.Serializable
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/processors")
internal class ProcessorResetRestApi(
    private val configuration: org.axonframework.common.configuration.Configuration
) {

    @GetMapping("/reset")
    fun resetProcessors(
        @RequestParam(defaultValue = "Sample") resetContext: String
    ): ResetProcessorsResponse {
        val processors = configuration.getComponents(PooledStreamingEventProcessor::class.java)
        val processorNames = mutableListOf<String>()

        processors.values.forEach { processor ->
            processor.shutdown().join()
            processor.resetTokens(CustomResetContext(resetContext)).join()
            processor.start().join()
            processorNames.add(processor.name())
        }

        return ResetProcessorsResponse(
            message = "Processors reset successfully",
            resetContext = resetContext,
            processorsReset = processorNames
        )
    }

    @Serializable
    data class ResetProcessorsResponse(
        val message: String,
        val resetContext: String,
        val processorsReset: List<String>
    )
}