package com.dddheroes.cinema

import org.axonframework.extensions.spring.test.AxonSpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@AxonSpringBootTest
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test", "testcontainers", "axonserver")
annotation class CinemaAxonSpringBootTest
