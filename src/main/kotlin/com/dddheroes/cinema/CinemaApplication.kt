package com.dddheroes.cinema

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CinemaApplication

fun main(args: Array<String>) {
    runApplication<CinemaApplication>(*args)
}
