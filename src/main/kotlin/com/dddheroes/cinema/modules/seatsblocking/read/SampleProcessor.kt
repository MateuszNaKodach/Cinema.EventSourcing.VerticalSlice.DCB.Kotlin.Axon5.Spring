package com.dddheroes.cinema.modules.seatsblocking.read

import com.dddheroes.cinema.CustomResetContext
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import org.axonframework.messaging.eventhandling.annotation.EventHandler
import org.axonframework.messaging.eventhandling.replay.annotation.ReplayContext
import org.springframework.stereotype.Component

@Component
class SampleProcessor {

    @EventHandler
    fun handle(event: SeatBlocked, @ReplayContext customResetContext: CustomResetContext?) {
        println("Received event: $event")
        println("Received context: $customResetContext")
    }
}