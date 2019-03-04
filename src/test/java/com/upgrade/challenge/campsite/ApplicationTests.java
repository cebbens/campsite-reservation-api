package com.upgrade.challenge.campsite;

import com.upgrade.challenge.campsite.api.CampsiteDto;
import com.upgrade.challenge.campsite.api.reservation.ReservationDto;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
public class ApplicationTests {

    private CampsiteDto campsiteDto;

    @BeforeClass
    public static void initRestAssured() {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .log(LogDetail.ALL)
                .setBasePath("/api/v1")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        RestAssured.responseSpecification = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    public void init() {
        campsiteDto = get("/campsites/{id}", UUID.fromString("9045b895-f634-4f2b-997c-fe5a2dbe8126"))
                .jsonPath().getObject("data", CampsiteDto.class);
    }

    @Test
    public void shouldCreateCampsite() {
        CampsiteDto createdCampsiteDto = CampsiteDto.builder().name("Created Test Campsite").capacity(10).build();

        UUID campsiteId =
            given()
                .body(createdCampsiteDto)
            .when()
                .post("/campsites")
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("message", is(HttpStatus.CREATED.getReasonPhrase()))
                .body("data.name", is(createdCampsiteDto.getName()))
                .body("data.capacity", is(createdCampsiteDto.getCapacity()))
                .extract().jsonPath().getUUID("data.id");

        expect().statusCode(HttpStatus.OK.value()).when().delete("/campsites/{id}", campsiteId);
    }

    @Test
    public void shouldNotCreateCampsiteWithoutCapacity() {
        given()
            .body(CampsiteDto.builder().name("Test Campsite").build())
        .when()
            .post("/campsites")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.size()", is(1))
            .body("errors[0]", is("Field 'capacity' must be specified"));
    }

    @Test
    public void shouldGetAllCampsites() {
        when()
            .get("/campsites")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.size()", is(1))
            .body("data[0].id", is(campsiteDto.getId().toString()));
    }

    @Test
    public void shouldGetCampsite() {
        when()
            .get("/campsites/{id}", campsiteDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.name", is(campsiteDto.getName()))
            .body("data.capacity", is(campsiteDto.getCapacity()));
    }

    @Test
    public void shouldUpdateCampsite() {
        CampsiteDto updatedCampsiteDto = CampsiteDto.builder().name("Updated Test Campsite").build();

        given()
            .body(updatedCampsiteDto)
        .when()
            .put("/campsites/{id}", campsiteDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.name", is(updatedCampsiteDto.getName()))
            .body("data.capacity", is(campsiteDto.getCapacity()));

        updatedCampsiteDto.setName("Test Campsite");

        expect().statusCode(HttpStatus.OK.value()).given().body(updatedCampsiteDto).when()
                .put("/campsites/{id}", campsiteDto.getId());
    }

    @Test
    public void shouldNotUpdateCampsiteWithCapacity() {
        given()
            .body(CampsiteDto.builder().name("Updated Test Campsite").capacity(10).build())
        .when()
            .put("/campsites/{id}", campsiteDto.getId())
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", is(HttpStatus.BAD_REQUEST.getReasonPhrase()))
            .body("errors.size()", is(1))
            .body("errors[0]", is("Field 'capacity' must not be specified"));
    }

    @Test
    public void shouldDeleteCampsite() {
        UUID campsiteId = given().body(CampsiteDto.builder().name("Delete Test Campsite").capacity(10).build())
                .post("/campsites").jsonPath().getUUID("data.id");

        when()
            .delete("/campsites/{id}", campsiteId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()));

        expect().statusCode(HttpStatus.NOT_FOUND.value()).when().get("/campsites/{id}", campsiteId);
    }

    @Test
    public void shouldGetAllCampsiteAvailabilities() {
        when()
            .get("/campsites/{id}/availabilities", campsiteDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.size()", greaterThan(0))
            .body("data[0].date", is(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("data[0].sites", is(campsiteDto.getCapacity()));
    }

    @Test
    public void shouldMakeCampsiteReservation() {
        ReservationDto reservationDto = this.makeCampsiteReservation();

        expect().statusCode(HttpStatus.OK.value()).when().delete("/campsites/{id}/reservations/{reservationId}",
                campsiteDto.getId(), reservationDto.getId());
    }

    @Test
    public void shouldGetAllCampsiteReservations() {
        ReservationDto reservationDto = this.makeCampsiteReservation();

        when()
            .get("/campsites/{id}/reservations", campsiteDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.size()", is(1))
            .body("data[0].id", is(reservationDto.getId().toString()));

        expect().statusCode(HttpStatus.OK.value()).when().delete("/campsites/{id}/reservations/{reservationId}",
                campsiteDto.getId(), reservationDto.getId());
    }

    @Test
    public void shouldGetCampsiteReservation() {
        ReservationDto reservationDto = this.makeCampsiteReservation();

        when()
            .get("/campsites/{id}/reservations/{reservationId}", campsiteDto.getId(), reservationDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.name", is(reservationDto.getName()))
            .body("data.email", is(reservationDto.getEmail()))
            .body("data.checkIn", is(reservationDto.getCheckIn().format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("data.checkOut", is(reservationDto.getCheckOut().format(DateTimeFormatter.ISO_LOCAL_DATE)));

        expect().statusCode(HttpStatus.OK.value()).when().delete("/campsites/{id}/reservations/{reservationId}",
                campsiteDto.getId(), reservationDto.getId());
    }

    @Test
    public void shouldUpdateCampsiteReservation() {
        ReservationDto reservationDto = this.makeCampsiteReservation();

        ReservationDto updatedReservationDto = ReservationDto.builder().name(reservationDto.getName())
                .email("john.doe2@test.com").checkIn(reservationDto.getCheckIn())
                .checkOut(reservationDto.getCheckOut()).build();

        given()
            .body(updatedReservationDto)
        .when()
            .put("/campsites/{id}/reservations/{reservationId}", campsiteDto.getId(), reservationDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()))
            .body("data.name", is(reservationDto.getName()))
            .body("data.email", is(updatedReservationDto.getEmail()))
            .body("data.checkIn", is(reservationDto.getCheckIn().format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("data.checkOut", is(reservationDto.getCheckOut().format(DateTimeFormatter.ISO_LOCAL_DATE)));

        expect().statusCode(HttpStatus.OK.value()).when().delete("/campsites/{id}/reservations/{reservationId}",
                campsiteDto.getId(), reservationDto.getId());
    }

    @Test
    public void shouldCancelCampsiteReservation() {
        ReservationDto reservationDto = this.makeCampsiteReservation();

        given()
            .body(reservationDto)
        .when()
            .delete("/campsites/{id}/reservations/{reservationId}", campsiteDto.getId(), reservationDto.getId())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("message", is(HttpStatus.OK.getReasonPhrase()));
    }

    private ReservationDto makeCampsiteReservation() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        ReservationDto reservationDto = ReservationDto.builder().name("John Doe").email("john.doe@test.com")
                .checkIn(tomorrow).checkOut(tomorrow.plusDays(1)).build();

        return
            given()
                .body(reservationDto)
            .when()
                .post("/campsites/{id}/reservations", campsiteDto.getId())
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("message", is(HttpStatus.CREATED.getReasonPhrase()))
                .body("data.name", is(reservationDto.getName()))
                .body("data.email", is(reservationDto.getEmail()))
                .body("data.checkIn", is(tomorrow.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("data.checkOut", is(tomorrow.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .extract().jsonPath().getObject("data", ReservationDto.class);
    }
}
