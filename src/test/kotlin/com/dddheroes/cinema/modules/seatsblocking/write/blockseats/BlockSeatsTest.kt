package com.dddheroes.cinema.modules.seatsblocking.write.blockseats

import com.dddheroes.cinema.MessagingSpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["slices.seatsblocking.write.blockseats.enabled=true"])
class BlockSeatsTest : MessagingSpringBootTest() {

    @Test
    fun `block seats if screening scheduled and seat placed, not-blocked`() {
    }
}