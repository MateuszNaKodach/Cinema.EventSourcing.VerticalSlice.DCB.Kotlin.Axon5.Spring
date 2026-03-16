package com.dddheroes.cinema

import org.axonframework.messaging.core.correlation.CorrelationDataProvider
import org.axonframework.messaging.core.correlation.MessageOriginProvider
import org.axonframework.messaging.eventhandling.GenericEventMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.time.Clock

@Configuration
internal class AxonFrameworkConfiguration {

    @Bean("defaultAxonObjectMapper")
    fun defaultAxonObjectMapper(modules: List<JacksonModule>): ObjectMapper =
        JsonMapper.builder()
            .findAndAddModules()
            .addModules(modules)
            .build()

    @Bean
    fun messageOriginProvider(): CorrelationDataProvider {
        return MessageOriginProvider()
    }

    @Bean
    @Suppress("DEPRECATION")
    fun clock(): Clock {
        val clock = Clock.systemUTC()
        GenericEventMessage.clock = clock
        return clock
    }
}
