package com.dddheroes.cinema.modules.seatsblocking.write.blockseatsdcb

import com.dddheroes.cinema.shared.valueobjects.ScreeningId
import com.dddheroes.cinema.shared.valueobjects.SeatNumber
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SeatBlockingPolicyTest {

    private val screeningId = ScreeningId.random()
    private val now = Instant.now()
    private val owner = "Reservation:owner-1"
    private val otherOwner = "Reservation:owner-2"

    @Nested
    inner class NoSingleEmptySeatPolicyTest {

        private val policy = SeatBlockingPolicy.NoSingleEmptySeat

        @Test
        fun `allows blocking when no single empty gap is created`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 0) to null,
                    SeatNumber(1, 1) to null,
                    SeatNumber(1, 2) to null,
                    SeatNumber(1, 3) to null,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 0), SeatNumber(1, 1)), owner, now, policy)

            assertNull(policy.verify(command, state))
        }

        @Test
        fun `rejects blocking when it would leave a single empty seat gap`() {
            // Row: [blocked, ?, blocked_by_command] — seat at column 1 becomes isolated
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 0) to owner,
                    SeatNumber(1, 1) to null,
                    SeatNumber(1, 2) to null,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 2)), owner, now, policy)

            val violation = policy.verify(command, state)
            assertNotNull(violation)
            assert(violation!!.policyName == "NoSingleEmptySeat")
            assert(violation.affectedSeats.contains(SeatNumber(1, 1)))
        }

        @Test
        fun `allows blocking adjacent seats without gap`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 0) to owner,
                    SeatNumber(1, 1) to null,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            assertNull(policy.verify(command, state))
        }

        @Test
        fun `rejects blocking when gap at row edge`() {
            // Row: [?, blocked_by_command] at columns 0,1 — seat at column 0 becomes isolated (left edge)
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 0) to null,
                    SeatNumber(1, 1) to null,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            val violation = policy.verify(command, state)
            assertNotNull(violation)
            assert(violation!!.affectedSeats.contains(SeatNumber(1, 0)))
        }

        @Test
        fun `expands boundary to include left and right neighbors`() {
            val boundary = ConsistencyBoundaryId(screeningId, setOf(SeatNumber(1, 5)))
            val expanded = policy.expandBoundary(boundary)

            assert(expanded.seats.contains(SeatNumber(1, 4)))
            assert(expanded.seats.contains(SeatNumber(1, 5)))
            assert(expanded.seats.contains(SeatNumber(1, 6)))
            assert(expanded.seats.size == 3)
        }

        @Test
        fun `does not expand beyond row edges`() {
            val boundary = ConsistencyBoundaryId(screeningId, setOf(SeatNumber(1, 0)))
            val expanded = policy.expandBoundary(boundary)

            assert(expanded.seats.contains(SeatNumber(1, 0)))
            assert(expanded.seats.contains(SeatNumber(1, 1)))
            assert(expanded.seats.size == 2)
        }
    }

    @Nested
    inner class CovidSpacingPolicyTest {

        private val policy = SeatBlockingPolicy.CovidSpacing

        @Test
        fun `allows blocking when no adjacent seats are blocked by others`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 1) to null,
                    SeatNumber(1, 2) to null,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            assertNull(policy.verify(command, state))
        }

        @Test
        fun `rejects blocking when adjacent seat is blocked by another owner`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 1) to null,
                    SeatNumber(1, 2) to otherOwner,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            val violation = policy.verify(command, state)
            assertNotNull(violation)
            assert(violation!!.policyName == "CovidSpacing")
            assert(violation.affectedSeats.contains(SeatNumber(1, 2)))
        }

        @Test
        fun `allows blocking adjacent to own blockade`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 1) to null,
                    SeatNumber(1, 2) to owner,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            assertNull(policy.verify(command, state))
        }

        @Test
        fun `rejects blocking when vertically adjacent seat is blocked by another owner`() {
            val state = State(
                blockadeBySeat = mapOf(
                    SeatNumber(1, 1) to null,
                    SeatNumber(2, 1) to otherOwner,
                ),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now, policy)

            val violation = policy.verify(command, state)
            assertNotNull(violation)
            assert(violation!!.affectedSeats.contains(SeatNumber(2, 1)))
        }

        @Test
        fun `expands boundary to include all four neighbors`() {
            val boundary = ConsistencyBoundaryId(screeningId, setOf(SeatNumber(5, 5)))
            val expanded = policy.expandBoundary(boundary)

            assert(expanded.seats.contains(SeatNumber(5, 4))) // left
            assert(expanded.seats.contains(SeatNumber(5, 6))) // right
            assert(expanded.seats.contains(SeatNumber(4, 5))) // upper
            assert(expanded.seats.contains(SeatNumber(6, 5))) // lower
            assert(expanded.seats.contains(SeatNumber(5, 5))) // original
            assert(expanded.seats.size == 5)
        }
    }

    @Nested
    inner class DefaultPolicyTest {

        @Test
        fun `default policy never rejects`() {
            val state = State(
                blockadeBySeat = mapOf(SeatNumber(1, 1) to null),
                screeningEndTime = now.plusSeconds(3600)
            )
            val command = BlockSeats(screeningId, setOf(SeatNumber(1, 1)), owner, now)

            assertNull(SeatBlockingPolicy.Default.verify(command, state))
        }

        @Test
        fun `default policy does not expand boundary`() {
            val boundary = ConsistencyBoundaryId(screeningId, setOf(SeatNumber(1, 1)))
            val expanded = SeatBlockingPolicy.Default.expandBoundary(boundary)

            assert(expanded === boundary)
        }
    }
}
