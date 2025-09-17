package com.dddheroes.cinema

import org.axonframework.configuration.ApplicationConfigurer
import org.axonframework.test.fixture.AxonTestFixture
import org.axonframework.test.fixture.MessagesRecordingConfigurationEnhancer
import org.axonframework.test.server.AxonServerContainer
import org.axonframework.test.server.AxonServerContainerUtils
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("testcontainers")
abstract class SpringBootIntegrationTest {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var axonServerContainer: AxonServerContainer

    @Autowired
    lateinit var configurer: ApplicationConfigurer

    lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        AxonServerContainerUtils.purgeEventsFromAxonServer(
            axonServerContainer.host,
            axonServerContainer.httpPort,
            "default",
            AxonServerContainerUtils.DCB_CONTEXT
        )
        logger.info(
            "Using Axon Server for integration test. UI is available at http://localhost:{}",
            axonServerContainer.httpPort
        )
        fixture = AxonTestFixture.with(configurer)
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun recordingEnhancer() = MessagesRecordingConfigurationEnhancer()
    }

}