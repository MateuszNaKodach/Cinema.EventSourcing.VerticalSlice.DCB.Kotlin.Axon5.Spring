package com.dddheroes.cinema.modules.seatsblocking.write.blockseats

import com.dddheroes.cinema.SpringBootIntegrationTest
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class BlockSeatsTest : SpringBootIntegrationTest() {

    @Test
    fun `test`() {
        val now = currentTime()

        val screeningId = ScreeningId.random()
        val seats = listOf(
            SeatNumber(1, 1),
            SeatNumber(1, 2),
            SeatNumber(1, 3)
        )
        val events = listOf(
            SeatPlaced(screeningId, seats[0], now),
            SeatPlaced(screeningId, seats[1], now),
            SeatPlaced(screeningId, seats[2], now),
        )
        val owner = aBlockadeOwner()
        val command = BlockSeats(screeningId, seats.toSet(), owner, now)

        val expectedEvents = seats.map { SeatBlocked(screeningId, it, owner, now) }

        fixture.given()
            .events(events)
            .`when`()
            .command(command)
            .then()
            .success()
            .events(expectedEvents)
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime() = Instant.now()

}