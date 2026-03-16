package com.dddheroes.cinema.modules.seatsblocking.write.unblockseats

import com.dddheroes.extensions.webmvc.test.RestAssuredMockMvcTest
import com.dddheroes.sdk.application.CommandHandlerResult.Failure
import com.dddheroes.sdk.application.CommandHandlerResult.Success
import io.restassured.http.ContentType
import io.restassured.module.mockmvc.kotlin.extensions.Given
import io.restassured.module.mockmvc.kotlin.extensions.Then
import io.restassured.module.mockmvc.kotlin.extensions.When
import org.axonframework.extensions.spring.test.AxonGatewaysMock
import org.axonframework.extensions.spring.test.AxonGatewaysMockTest
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.time.Instant

@RestAssuredMockMvcTest
@AxonGatewaysMockTest
@TestPropertySource(properties = ["slices.seatsblocking.write.unblockseats.enabled=true"])
class UnblockSeatsRestApiTest @Autowired constructor(val gateways: AxonGatewaysMock) {

    @BeforeEach
    fun setUp() {
        gateways.currentTimeIs(Instant.now())
    }

    @Test
    fun `command success - returns 204 No Content`() {
        gateways.assumeCommandReturns<UnblockSeats>(Success)

        Given {
            contentType(ContentType.JSON)
            body("""{"seats": ["1:1", "1:2"], "blockadeOwner": "Reservation:123"}""")
        } When {
            async().delete("/screenings/screening-1/seats-blockades")
        } Then {
            statusCode(HttpStatus.NO_CONTENT.value())
        }
    }

    @Test
    fun `command failure - returns 400 Bad Request`() {
        gateways.assumeCommandReturns<UnblockSeats>(Failure("Cannot unblock seats - some seats are blocked by others: [1:1]"))

        Given {
            contentType(ContentType.JSON)
            body("""{"seats": ["1:1", "1:2"], "blockadeOwner": "Reservation:123"}""")
        } When {
            async().delete("/screenings/screening-1/seats-blockades")
        } Then {
            statusCode(HttpStatus.BAD_REQUEST.value())
            contentType(ContentType.JSON)
            body("message", equalTo("Cannot unblock seats - some seats are blocked by others: [1:1]"))
        }
    }
}
