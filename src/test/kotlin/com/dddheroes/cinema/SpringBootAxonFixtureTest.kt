package com.dddheroes.cinema

import org.axonframework.test.fixture.AxonTestFixture
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired

@CinemaAxonSpringBootTest
abstract class SpringBootAxonFixtureTest {

    @Autowired
    lateinit var fixture: AxonTestFixture

    @AfterEach
    fun afterEach() {
        fixture.stop()
    }
}
