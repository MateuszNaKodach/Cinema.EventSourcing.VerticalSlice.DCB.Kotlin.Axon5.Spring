package com.dddheroes.cinema.modules.seatsblocking.write.blockseats

import com.dddheroes.cinema.SpringBootIntegrationTest
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.events.ScreeningScheduled
import com.dddheroes.cinema.modules.dayschedule.random
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

class BlockSeatsTest : SpringBootIntegrationTest() {

    @Test
    fun `block seats if screening scheduled and seat placed, not-blocked`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seatRow1Col3 = SeatNumber(1, 3)
        val seats = listOf(seatRow1Col1, seatRow1Col2, seatRow1Col3)

        val owner = aBlockadeOwner()

        fixture.given()
            .events(
                ScreeningScheduled(
                    dayScheduleId,
                    screeningId,
                    movieId,
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    now
                ),
                SeatPlaced(screeningId, seatRow1Col1, now),
                SeatPlaced(screeningId, seatRow1Col2, now),
                SeatPlaced(screeningId, seatRow1Col3, now),
            )
            .`when`()
            .command(
                BlockSeats(screeningId, seats.toSet(), owner, now)
            )
            .then()
            .success()
            .events(
                SeatBlocked(screeningId, seatRow1Col1, owner, now),
                SeatBlocked(screeningId, seatRow1Col2, owner, now),
                SeatBlocked(screeningId, seatRow1Col3, owner, now),
            )
    }

    @Test
    fun `cannot block seats if screening has not been scheduled yet`() {
        val now = currentTime()

        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seatRow1Col3 = SeatNumber(1, 3)
        val seats = listOf(seatRow1Col1, seatRow1Col2, seatRow1Col3)

        val owner = aBlockadeOwner()

        fixture.given()
            .events(
                SeatPlaced(screeningId, seatRow1Col1, now),
                SeatPlaced(screeningId, seatRow1Col2, now),
                SeatPlaced(screeningId, seatRow1Col3, now),
            )
            .`when`()
            .command(
                BlockSeats(screeningId, seats.toSet(), owner, now)
            )
            .then()
            .exception(IllegalStateException::class.java, "Cannot block seats - screening not scheduled yet")
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime() = Instant.now()

    private fun currentTime(date: LocalDate, time: LocalTime = LocalTime.of(0, 0)) = date.atTime(time)
        .toInstant(ZoneOffset.UTC)

}