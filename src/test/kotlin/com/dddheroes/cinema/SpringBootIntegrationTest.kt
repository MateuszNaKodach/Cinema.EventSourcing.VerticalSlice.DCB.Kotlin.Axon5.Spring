package com.dddheroes.cinema

import org.axonframework.common.configuration.ApplicationConfigurer
import org.axonframework.test.fixture.AxonTestFixture
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(value = [SpringBootIntegrationTest.TestConfig::class, TestcontainersConfiguration::class])
@ActiveProfiles("test", "testcontainers", "axonserver")
// @ActiveProfiles("test", "axonserver") // if you don't want to use testcontainers
abstract class SpringBootIntegrationTest {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var configurer: ApplicationConfigurer

    lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        fixture = AxonTestFixture.with(configurer)
    }

    @AfterEach
    fun afterEach(){
        fixture.stop()
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun recordingEnhancer() = MessagesRecordingConfigurationEnhancer()
    }

}