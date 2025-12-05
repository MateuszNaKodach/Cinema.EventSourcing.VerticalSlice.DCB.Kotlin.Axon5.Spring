package com.dddheroes.cinema

import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.eventsourcing.eventstore.SourcingCondition
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.core.MessageStream
import org.axonframework.messaging.core.unitofwork.UnitOfWorkFactory
import org.axonframework.messaging.eventhandling.EventMessage
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(value = [TestcontainersConfiguration::class])
@ActiveProfiles("test", "testcontainers", "axonserver")
// @ActiveProfiles("test", "axonserver") // if you don't want to use testcontainers
abstract class MessagingSpringBootTest {

    @Spy
    @Autowired
    lateinit var commandGateway: CommandGateway

    @Autowired
    lateinit var eventStore: EventStore

    @Spy
    @Autowired
    lateinit var queryGateway: QueryGateway

    @Autowired
    lateinit var unitOfWorkFactory: UnitOfWorkFactory

    fun sourcedEvents(condition: SourcingCondition) {
        val unitOfWork = unitOfWorkFactory.create()
//        val stream: MessageStream<EventMessage> = unitOfWork.executeWithResult { ctx -> eventStore.transaction(ctx).source(condition) }.join()
    }

}