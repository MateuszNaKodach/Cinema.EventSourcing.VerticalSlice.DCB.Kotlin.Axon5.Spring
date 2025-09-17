package com.dddheroes.cinema.shared.valueobjects

data class SeatNumber(val row: Int, val column: Int) {

    init {
        require(row in MIN_SEAT_ROW..MAX_SEAT_ROW) { "Row must be between 0 and 9" }
        require(column in MIN_SEAT_COLUMN..MAX_SEAT_COLUMN) { "Column must be between 0 nad 9" }
    }

    override fun toString(): String = "$row:$column"

    companion object {
        // Assuming a cinema with 10 rows and 10 columns (0-9), just for the example simplicity
        const val MIN_SEAT_ROW = 0
        const val MAX_SEAT_ROW = 9

        const val MIN_SEAT_COLUMN = 0
        const val MAX_SEAT_COLUMN = 9

        fun from(raw: String): SeatNumber {
            val split = raw.split(":")
            return SeatNumber(split[0].toInt(), split[1].toInt())
        }
    }
}
