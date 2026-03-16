package com.dddheroes.cinema.modules.seatsblocking.write.unblockseats

import com.dddheroes.cinema.CinemaAxonSpringBootTest
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.events.ScreeningScheduled
import com.dddheroes.cinema.modules.dayschedule.random
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatPlaced
import com.dddheroes.cinema.modules.seatsblocking.events.SeatUnblocked
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

@CinemaAxonSpringBootTest
@TestPropertySource(properties = ["slices.seatsblocking.write.unblockseats.enabled=true"])
class UnblockSeatsSpringSliceTest @Autowired constructor(val sliceUnderTest: AxonTestFixture) {

    @Test
    fun `unblock previously blocked seats by same owner`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seatRow1Col3 = SeatNumber(1, 3)
        val seats = listOf(seatRow1Col1, seatRow1Col2, seatRow1Col3)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
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
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                    SeatBlocked(screeningId, seatRow1Col3, owner, now),
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatUnblocked(screeningId, seatRow1Col1, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col2, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col3, owner, now),
                )
            }
        }
    }

    @Test
    fun `nothing happens when trying to unblock already unblocked seats (idempotent)`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seats = listOf(seatRow1Col1, seatRow1Col2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
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
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                noEvents()
            }
        }
    }

    @Test
    fun `cannot unblock seats blocked by different owner`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seats = listOf(seatRow1Col1, seatRow1Col2)

        val ownerA = aBlockadeOwner()
        val ownerB = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
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
                    SeatBlocked(screeningId, seatRow1Col1, ownerA, now),
                    SeatBlocked(screeningId, seatRow1Col2, ownerA, now),
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), ownerB, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Cannot unblock seats - some seats are blocked by others: [1:1, 1:2]"))
            }
        }
    }

    @Test
    fun `cannot unblock non-placed seats`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow5Col1 = SeatNumber(5, 1)
        val seatRow5Col2 = SeatNumber(5, 2)
        val seats = listOf(seatRow5Col1, seatRow5Col2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(
                        dayScheduleId,
                        screeningId,
                        movieId,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        now
                    ),
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Cannot unblock seats - must be placed first"))
            }
        }
    }

    @Test
    fun `unblock only blocked seats when some are already unblocked (partial + idempotent)`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seatRow1Col3 = SeatNumber(1, 3)
        val seats = listOf(seatRow1Col1, seatRow1Col2, seatRow1Col3)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
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
                    // Only 2 seats are blocked
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatUnblocked(screeningId, seatRow1Col1, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col2, owner, now),
                )
            }
        }
    }

    @Test
    fun `unblock seats that were re-blocked after being unblocked`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seats = listOf(seatRow1Col1, seatRow1Col2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
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
                    // Blocked, then unblocked, then blocked again
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col1, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col2, owner, now),
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                )
            } When {
                command(UnblockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatUnblocked(screeningId, seatRow1Col1, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col2, owner, now),
                )
            }
        }
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime(date: LocalDate, time: LocalTime = LocalTime.of(0, 0)) = date.atTime(time)
        .toInstant(ZoneOffset.UTC)
}
