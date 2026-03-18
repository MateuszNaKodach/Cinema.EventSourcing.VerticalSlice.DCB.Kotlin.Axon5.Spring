package com.dddheroes.cinema.modules.seatsblocking.write.blockseat

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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

@RestAssuredMockMvcTest
@AxonGatewaysMockTest
@TestPropertySource(properties = ["slices.seatsblocking.write.blockseat.enabled=true"])
class BlockSeatRestApiTest @Autowired constructor(val gateways: AxonGatewaysMock) {

    @Test
    fun `command success - returns 204 No Content`() {
        gateways.assumeCommandReturns<BlockSeat>(Success)

        Given {
            contentType(ContentType.JSON)
            body("""{"blockadeOwner": "Reservation:123"}""")
        } When {
            async().put("/screenings/screening-1/seats-blockades/1:1")
        } Then {
            statusCode(HttpStatus.NO_CONTENT.value())
        }
    }

    @Test
    fun `command failure - returns 400 Bad Request`() {
        gateways.assumeCommandReturns<BlockSeat>(Failure("Seat is already blocked by Reservation:456"))

        Given {
            contentType(ContentType.JSON)
            body("""{"blockadeOwner": "Reservation:123"}""")
        } When {
            async().put("/screenings/screening-1/seats-blockades/1:1")
        } Then {
            statusCode(HttpStatus.BAD_REQUEST.value())
            contentType(ContentType.JSON)
            body("message", equalTo("Seat is already blocked by Reservation:456"))
        }
    }
}
