package com.dddheroes.cinema

import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.KotlinSerializationConverter
import com.dddheroes.cinema.modules.seatsblocking.write.blockseats.BlockSeats
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import org.axonframework.conversion.Converter
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.inmemory.InMemoryTokenStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.time.Instant

@SpringBootApplication
class CinemaApplication

fun main(args: Array<String>) {
    runApplication<CinemaApplication>(*args)
}

@Configuration
class EventProcessingConfiguration {

    @Bean
    fun tokenStore(): TokenStore {
        return InMemoryTokenStore()
    }

    @Primary
    @Bean
    fun converter(): Converter = KotlinSerializationConverter()

}

@Service
class DataInit(val commandGateway: CommandGateway) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        println("Data initialization")
        commandGateway.sendAndWait(
            BlockSeats(
                screeningId = ScreeningId.of("1"),
                seats = setOf(SeatNumber(1, 1)),
                blockadeOwner = "Test",
                issuedAt = Instant.now()
            )
        )
    }
}
