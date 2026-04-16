package com.bdd.steps;

import com.bdd.config.ConfigManager;
import com.bdd.hooks.Hooks;
import com.bdd.utils.BaseRequestSpec;
import com.bdd.utils.BookingPayloadBuilder;
import com.bdd.utils.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class BookingSteps {

    private static final Logger log = LoggerFactory.getLogger(BookingSteps.class);
    private final ScenarioContext context;

    public BookingSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("I request all bookings")
    public void iRequestAllBookings() {
        Response response = BaseRequestSpec.getBaseSpec()
                .when()
                .get("/booking")
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @When("I request the booking by ID")
    public void iRequestBookingById() {
        Response response = BaseRequestSpec.getBaseSpec()
                .when()
                .get("/booking/" + context.getBookingId())
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @When("I update the booking with new details")
    public void iUpdateTheBooking() {
        String payload = BookingPayloadBuilder.buildWith(
                "UpdatedFirst", "UpdatedLast",
                999, true,
                "2025-06-01", "2025-06-10",
                "Breakfast"
        );
        context.setLastRequestPayload(payload);

        Response response = BaseRequestSpec.getAuthSpec(Hooks.authToken)
                .body(payload)
                .when()
                .put("/booking/" + context.getBookingId())
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @When("I partially update the booking firstname to {string}")
    public void iPartiallyUpdateBooking(String firstname) {
        String payload = String.format("{\"firstname\": \"%s\"}", firstname);
        context.setLastRequestPayload(payload);

        Response response = BaseRequestSpec.getAuthSpec(Hooks.authToken)
                .body(payload)
                .when()
                .patch("/booking/" + context.getBookingId())
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @When("I delete the booking")
    public void iDeleteTheBooking() {
        Response response = BaseRequestSpec.getAuthSpec(Hooks.authToken)
                .when()
                .delete("/booking/" + context.getBookingId())
                .then()
                .extract().response();

        context.setLastResponse(response);
        // Reset so @After hook skips double-delete
        context.setBookingId(-1);
        assertSla(response);
    }

    @When("I create a booking with firstname {string} lastname {string} price {int} deposit {string} checkin {string} checkout {string}")
    public void iCreateABooking(String firstname, String lastname, int price,
                                String deposit, String checkin, String checkout) {
        String payload = BookingPayloadBuilder.buildWith(
                firstname, lastname, price,
                Boolean.parseBoolean(deposit),
                checkin, checkout, "None"
        );
        context.setLastRequestPayload(payload);

        Response response = BaseRequestSpec.getAuthSpec(Hooks.authToken)
                .body(payload)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @And("the response should contain a list of bookings")
    public void theResponseShouldContainListOfBookings() {
        List<?> bookings = context.getLastResponse().jsonPath().getList("$");
        assertNotNull(bookings, "Booking list is null");
        assertFalse(bookings.isEmpty(), "Booking list is empty");
        log.info("Booking list returned {} entries", bookings.size());
    }

    @And("the response should contain valid booking details")
    public void theResponseShouldContainValidBookingDetails() {
        String firstname = context.getLastResponse().jsonPath().getString("firstname");
        assertNotNull(firstname, "Booking firstname is null");
        log.info("Booking details valid — firstname: {}", firstname);
    }

    @And("the response should reflect the updated details")
    public void theResponseShouldReflectUpdatedDetails() {
        String firstname = context.getLastResponse().jsonPath().getString("firstname");
        assertEquals(firstname, "UpdatedFirst", "Firstname not updated");
    }

    @And("the response firstname should be {string}")
    public void theResponseFirstnameShouldBe(String expected) {
        String firstname = context.getLastResponse().jsonPath().getString("firstname");
        assertEquals(firstname, expected, "Firstname mismatch after PATCH");
    }

    @And("the response should contain a valid booking ID")
    public void theResponseShouldContainValidBookingId() {
        int id = context.getLastResponse().jsonPath().getInt("bookingid");
        assertTrue(id > 0, "Booking ID is invalid: " + id);
        log.info("Created booking ID: {}", id);
    }

    private void assertSla(Response response) {
        long responseTime = response.getTime();
        assertTrue(
                responseTime <= ConfigManager.get().slaResponseTimeMs(),
                String.format("SLA breach: %dms > %dms", responseTime,
                        ConfigManager.get().slaResponseTimeMs())
        );
    }
}