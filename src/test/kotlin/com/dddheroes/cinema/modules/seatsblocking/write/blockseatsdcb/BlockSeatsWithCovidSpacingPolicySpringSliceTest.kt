package com.dddheroes.cinema.modules.seatsblocking.write.blockseatsdcb

import com.dddheroes.cinema.CinemaAxonSpringBootTest
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.events.ScreeningScheduled
import com.dddheroes.cinema.modules.dayschedule.random
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import com.dddheroes.sdk.application.CommandHandlerResult
import org.axonframework.test.fixture.AxonTestFixture
import org.axonframework.test.fixture.Given
import org.axonframework.test.fixture.Scenario
import org.axonframework.test.fixture.Then
import org.axonframework.test.fixture.When
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

@CinemaAxonSpringBootTest
@TestPropertySource(properties = ["slices.seatsblocking.write.blockseats.enabled=true"])
class BlockSeatsWithCovidSpacingPolicySpringSliceTest @Autowired constructor(val sliceUnderTest: AxonTestFixture) {

    private val policy = SeatBlockingPolicy.CovidSpacing

    @Test
    fun `block seats when no adjacent seats are blocked by others`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(3, 3)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat), owner, now, policy))
            } Then {
                success()
                events(SeatBlocked(screeningId, seat, owner, now))
            }
        }
    }

    @Test
    fun `cannot block seat adjacent to seat blocked by another owner`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(3, 3)
        val adjacentSeat = SeatNumber(3, 4)

        val ownerA = aBlockadeOwner()
        val ownerB = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat, now),
                    SeatPlaced(screeningId, adjacentSeat, now),
                    SeatBlocked(screeningId, adjacentSeat, ownerA, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat), ownerB, now, policy))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("[CovidSpacing] Seat 3:3 is adjacent to seats blocked by others: [3:4]"))
            }
        }
    }

    @Test
    fun `cannot block seat vertically adjacent to seat blocked by another owner`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(3, 3)
        val seatAbove = SeatNumber(2, 3)

        val ownerA = aBlockadeOwner()
        val ownerB = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat, now),
                    SeatPlaced(screeningId, seatAbove, now),
                    SeatBlocked(screeningId, seatAbove, ownerA, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat), ownerB, now, policy))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("[CovidSpacing] Seat 3:3 is adjacent to seats blocked by others: [2:3]"))
            }
        }
    }

    @Test
    fun `allow blocking adjacent to own blockade`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(3, 3)
        val adjacentSeat = SeatNumber(3, 4)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat, now),
                    SeatPlaced(screeningId, adjacentSeat, now),
                    SeatBlocked(screeningId, adjacentSeat, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat), owner, now, policy))
            } Then {
                success()
                events(SeatBlocked(screeningId, seat, owner, now))
            }
        }
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime(date: LocalDate, time: LocalTime = LocalTime.of(0, 0)) = date.atTime(time)
        .toInstant(ZoneOffset.UTC)
}
