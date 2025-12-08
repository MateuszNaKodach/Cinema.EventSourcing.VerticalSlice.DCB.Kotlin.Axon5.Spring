package com.dddheroes.cinema

import com.dddheroes.cinema.infrastructure.axon.conversion.kotlinserialization.KotlinSerializationConverter
import com.dddheroes.cinema.modules.seatsblocking.write.blockseats.BlockSeats
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.Serializable
import org.axonframework.common.jdbc.PersistenceExceptionResolver
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.conversion.Converter
import org.axonframework.conversion.json.JacksonConverter
import org.axonframework.eventsourcing.eventstore.jpa.SQLErrorCodesResolver
import org.axonframework.extension.springboot.TokenStoreProperties
import org.axonframework.extension.springboot.util.jpa.ContainerManagedEntityManagerProvider
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.JpaTokenStore
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.JpaTokenStoreConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.sql.SQLException
import java.time.Instant
import javax.sql.DataSource

@SpringBootApplication
class CinemaApplication

fun main(args: Array<String>) {
    runApplication<CinemaApplication>(*args)
}

@Configuration
class EventProcessingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun entityManagerProvider(): EntityManagerProvider {
        return ContainerManagedEntityManagerProvider()
    }

    /**
     * Builds a JPA Token Store.
     *
     * @param entityManagerProvider   An entity manager provider to retrieve connections.
     * @param tokenStoreProperties    A set of properties to configure the token store.
     * @param defaultAxonObjectMapper An object mapper to use for token conversion to JSON.
     * @return Instance of JPA token store.
     */
    @Bean
    @ConditionalOnMissingBean
    fun tokenStore(
        entityManagerProvider: EntityManagerProvider,
//        tokenStoreProperties: TokenStoreProperties,
        defaultAxonObjectMapper: ObjectMapper
    ): TokenStore {
        val config = JpaTokenStoreConfiguration.DEFAULT
        val converter = JacksonConverter(defaultAxonObjectMapper)
        return JpaTokenStore(entityManagerProvider, converter, config)
    }

    /**
     * Provides a persistence exception resolver for a data source.
     *
     * @param dataSource A data source configured to resolve exception for.
     * @return A working copy of Persistence Exception Resolver.
     * @throws SQLException on any construction errors.
     */
    @Bean
    @ConditionalOnMissingBean
    @Throws(SQLException::class)
    fun persistenceExceptionResolver(dataSource: DataSource): PersistenceExceptionResolver {
        return SQLErrorCodesResolver(dataSource)
    }

    @Primary
    @Bean
    fun converter(): Converter = KotlinSerializationConverter()

}

@Service
class DataInit(
    val commandGateway: CommandGateway,
) : ApplicationListener<ApplicationReadyEvent> {

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

@Serializable
data class CustomResetContext(val data: String)
