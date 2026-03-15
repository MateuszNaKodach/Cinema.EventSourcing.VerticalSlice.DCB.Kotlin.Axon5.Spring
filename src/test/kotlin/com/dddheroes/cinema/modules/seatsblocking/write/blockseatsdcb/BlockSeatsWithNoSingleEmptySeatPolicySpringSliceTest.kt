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
class BlockSeatsWithNoSingleEmptySeatPolicySpringSliceTest @Autowired constructor(val sliceUnderTest: AxonTestFixture) {

    private val policy = SeatBlockingPolicy.NoSingleEmptySeat

    @Test
    fun `block seat adjacent to already blocked without creating gap`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat0 = SeatNumber(1, 0)
        val seat1 = SeatNumber(1, 1)
        val seat2 = SeatNumber(1, 2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat0, now),
                    SeatPlaced(screeningId, seat1, now),
                    SeatPlaced(screeningId, seat2, now),
                    SeatBlocked(screeningId, seat0, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat1), owner, now, policy))
            } Then {
                success()
                events(SeatBlocked(screeningId, seat1, owner, now))
            }
        }
    }

    @Test
    fun `cannot block seats when it would leave a single empty gap`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat0 = SeatNumber(1, 0)
        val seat1 = SeatNumber(1, 1)
        val seat2 = SeatNumber(1, 2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat0, now),
                    SeatPlaced(screeningId, seat1, now),
                    SeatPlaced(screeningId, seat2, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat0, seat2), owner, now, policy))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("[NoSingleEmptySeat] Blocking would leave seat 1:1 as a single empty gap"))
            }
        }
    }

    @Test
    fun `cannot block seat when it would leave gap next to already blocked seat`() {
        val dayScheduleId = DayScheduleId.random()
        val now = currentTime(dayScheduleId.toLocalDate(), LocalTime.of(10, 0))

        val movieId = MovieId.random()
        val screeningId = ScreeningId.random()
        val seat0 = SeatNumber(1, 0)
        val seat1 = SeatNumber(1, 1)
        val seat2 = SeatNumber(1, 2)

        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    ScreeningScheduled(dayScheduleId, screeningId, movieId, LocalTime.of(10, 0), LocalTime.of(12, 0), now),
                    SeatPlaced(screeningId, seat0, now),
                    SeatPlaced(screeningId, seat1, now),
                    SeatPlaced(screeningId, seat2, now),
                    SeatBlocked(screeningId, seat0, owner, now),
                )
            } When {
                command(BlockSeats(screeningId, setOf(seat2), owner, now, policy))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("[NoSingleEmptySeat] Blocking would leave seat 1:1 as a single empty gap"))
            }
        }
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"

    private fun currentTime(date: LocalDate, time: LocalTime = LocalTime.of(0, 0)) = date.atTime(time)
        .toInstant(ZoneOffset.UTC)
}
