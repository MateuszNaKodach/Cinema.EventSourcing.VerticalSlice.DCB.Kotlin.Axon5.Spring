package com.dddheroes.cinema

import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@Import(value = [TestcontainersConfiguration::class])
@ActiveProfiles("test", "testcontainers", "axonserver")
// @ActiveProfiles("test", "axonserver") // if you don't want to use testcontainers
abstract class MessagingSpringBootTest {

    @MockitoSpyBean
    @Autowired
    lateinit var commandGateway: CommandGateway

    @Autowired
    lateinit var eventStore: EventStore

    @MockitoSpyBean
    @Autowired
    lateinit var queryGateway: QueryGateway

}