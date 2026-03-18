package com.dddheroes.cinema.modules.seatsblocking.write.blockseat

import com.dddheroes.cinema.CinemaAxonSpringBootTest
import com.dddheroes.cinema.modules.seatsblocking.events.SeatBlocked
import com.dddheroes.cinema.modules.seatsblocking.events.SeatNotBlocked
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
import java.util.*

@CinemaAxonSpringBootTest
@TestPropertySource(properties = ["slices.seatsblocking.write.blockseat.enabled=true"])
class BlockSeatSpringSliceTest @Autowired constructor(val sliceUnderTest: AxonTestFixture) {

    @Test
    fun `block placed seat for the first time - success`() {
        val now = Instant.now()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(1, 1)
        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(SeatPlaced(screeningId, seat, now))
            } When {
                command(BlockSeat(screeningId, seat, owner, now))
            } Then {
                success()
                events(SeatBlocked(screeningId, seat, owner, now))
            }
        }
    }

    @Test
    fun `block previously unblocked seat - success`() {
        val now = Instant.now()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(1, 1)
        val firstOwner = aBlockadeOwner()
        val secondOwner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    SeatPlaced(screeningId, seat, now),
                    SeatBlocked(screeningId, seat, firstOwner, now),
                    SeatUnblocked(screeningId, seat, firstOwner, now),
                )
            } When {
                command(BlockSeat(screeningId, seat, secondOwner, now))
            } Then {
                success()
                events(SeatBlocked(screeningId, seat, secondOwner, now))
            }
        }
    }

    @Test
    fun `same owner blocking already blocked seat - idempotent, no events`() {
        val now = Instant.now()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(1, 1)
        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    SeatPlaced(screeningId, seat, now),
                    SeatBlocked(screeningId, seat, owner, now),
                )
            } When {
                command(BlockSeat(screeningId, seat, owner, now))
            } Then {
                success()
                noEvents()
            }
        }
    }

    @Test
    fun `different owner blocking already blocked seat - failure`() {
        val now = Instant.now()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(1, 1)
        val firstOwner = aBlockadeOwner()
        val secondOwner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given {
                events(
                    SeatPlaced(screeningId, seat, now),
                    SeatBlocked(screeningId, seat, firstOwner, now),
                )
            } When {
                command(BlockSeat(screeningId, seat, secondOwner, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Seat is already blocked by $firstOwner"))
                events(SeatNotBlocked(screeningId, seat, "Seat is already blocked by $firstOwner", secondOwner, now))
            }
        }
    }

    @Test
    fun `blocking non-placed seat - failure`() {
        val now = Instant.now()
        val screeningId = ScreeningId.random()
        val seat = SeatNumber(1, 1)
        val owner = aBlockadeOwner()

        sliceUnderTest.Scenario {
            Given { } When {
                command(BlockSeat(screeningId, seat, owner, now))
            } Then {
                resultMessagePayload(CommandHandlerResult.Failure("Seat must be placed before it can be blocked"))
                events(SeatNotBlocked(screeningId, seat, "Seat must be placed before it can be blocked", owner, now))
            }
        }
    }

    private fun aBlockadeOwner() = "Reservation:${UUID.randomUUID()}"
}
