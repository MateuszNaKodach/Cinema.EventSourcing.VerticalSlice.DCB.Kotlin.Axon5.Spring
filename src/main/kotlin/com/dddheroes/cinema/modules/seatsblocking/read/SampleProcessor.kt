package com.dddheroes.cinema.modules.seatsblocking.read

import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import org.axonframework.messaging.eventhandling.annotation.EventHandler
import org.springframework.stereotype.Component

@Component
class SampleProcessor {

    @EventHandler
    fun handle(event: SeatBlocked) {
        println("Received event: $event")
    }
}