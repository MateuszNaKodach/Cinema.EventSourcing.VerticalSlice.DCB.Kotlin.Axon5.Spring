package com.dddheroes.cinema.modules.seatsblocking.write.blockseats

import com.dddheroes.cinema.CinemaAxonSpringBootTest
import com.dddheroes.cinema.modules.dayschedule.DayScheduleId
import com.dddheroes.cinema.modules.dayschedule.MovieId
import com.dddheroes.cinema.modules.dayschedule.events.ScreeningScheduled
import com.dddheroes.cinema.modules.dayschedule.random
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*

@CinemaAxonSpringBootTest
@TestPropertySource(properties = ["slices.seatsblocking.write.blockseats.enabled=true"])
class BlockSeatsSpringSliceTest @Autowired constructor(val sliceUnderTest: AxonTestFixture) {

    @Test
    fun `block seats as first event when screening scheduled`() {
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
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                    SeatBlocked(screeningId, seatRow1Col3, owner, now),
                )
            }
        }
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

        sliceUnderTest.Scenario {
            Given {
                noPriorActivity()
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Cannot block seats - screening not scheduled yet"))
            }
        }
    }

    @Test
    fun `cannot block seats if screening has already ended`() {
        val dayScheduleId = DayScheduleId.random()
        val screeningStartTime = LocalTime.of(10, 0)
        val screeningEndTime = LocalTime.of(12, 0)
        val eventTime = currentTime(dayScheduleId.toLocalDate(), screeningStartTime)
        val afterScreeningEndTime = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(13, 0))

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
                        screeningStartTime,
                        screeningEndTime,
                        eventTime
                    ),
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, afterScreeningEndTime))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Cannot block seats - screening has already ended"))
            }
        }
    }

    @Test
    fun `cannot block seats if they are already blocked by others`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seatRow1Col1 = SeatNumber(1, 1)
        val seatRow1Col2 = SeatNumber(1, 2)
        val seatRow1Col3 = SeatNumber(1, 3)
        val seats = listOf(seatRow1Col1, seatRow1Col2, seatRow1Col3)

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
                    SeatBlocked(screeningId, seatRow1Col1, ownerA, now),
                    SeatBlocked(screeningId, seatRow1Col2, ownerA, now),
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), ownerB, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Cannot block seats - some seats are already blocked by others: [1:1, 1:2]"))
            }
        }
    }

    @Test
    fun `nothing happens, when trying to block seats already blocked by same owner`() {
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
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                noEvents()
            }
        }
    }

    @Test
    fun `block only available seats when some seats are already blocked by same owner`() {
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
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                    SeatBlocked(screeningId, seatRow1Col3, owner, now),
                )
            }
        }
    }

    @Test
    fun `block seats that were previously unblocked`() {
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
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col1, owner, now),
                    SeatUnblocked(screeningId, seatRow1Col2, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, seats.toSet(), owner, now))
            } Then {
                success()
                events(
                    SeatBlocked(screeningId, seatRow1Col1, owner, now),
                    SeatBlocked(screeningId, seatRow1Col2, owner, now),
                )
            }
        }
    }

    @Test
    fun `blocking seats for one screening does not interfere with another screening`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningA = ScreeningId.random()
        val screeningB = ScreeningId.random()
        val sameSeat = SeatNumber(1, 1)

        val ownerA = aBlockadeOwner()
        val ownerB = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(
                        dayScheduleId,
                        screeningA,
                        movieId,
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        now
                    ),
                    SeatBlocked(screeningA, sameSeat, ownerA, now),
                    ScreeningScheduled(
                        dayScheduleId,
                        screeningB,
                        movieId,
                        LocalTime.of(14, 0),
                        LocalTime.of(16, 0),
                        now
                    ),
                )
            } When {
                command(BlockSeats(screeningB, setOf(sameSeat), ownerB, now))
            } Then {
                success()
                events(
                    SeatBlocked(screeningB, sameSeat, ownerB, now),
                )
            }
        }
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime() = java.time.Instant.now()

    private fun currentTime(date: LocalDate, time: LocalTime = LocalTime.of(0, 0)) = date.atTime(time)
        .toInstant(ZoneOffset.UTC)
}
