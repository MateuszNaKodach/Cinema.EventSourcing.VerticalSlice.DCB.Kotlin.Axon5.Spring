package com.dddheroes.cinema

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<CinemaApplication>().with(TestcontainersConfiguration::class).run(*args)
}
