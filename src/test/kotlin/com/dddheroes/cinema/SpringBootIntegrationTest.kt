package com.dddheroes.cinema

import org.axonframework.configuration.ApplicationConfigurer
import org.axonframework.test.fixture.AxonTestFixture
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@Import(value = [SpringBootIntegrationTest.TestConfig::class])
@SpringBootTest
abstract class SpringBootIntegrationTest {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var configurer: ApplicationConfigurer

    lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        fixture = AxonTestFixture.with(configurer)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun recordingEnhancer() = MessagesRecordingConfigurationEnhancer()
    }

}