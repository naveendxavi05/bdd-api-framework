package com.bdd.hooks;

import com.bdd.utils.BookingPayloadBuilder;
import com.bdd.config.ConfigManager;
import com.bdd.utils.BaseRequestSpec;
import com.bdd.utils.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    // Auth token fetched ONCE for the entire suite — shared across all scenarios
    public static String authToken;

    private final ScenarioContext context;

    // PicoContainer injects ScenarioContext here
    public Hooks(ScenarioContext context) {
        this.context = context;
    }

    // ── @BeforeAll ────────────────────────────────────────────────────────────
    // Runs once before any scenario. Health check + token fetch.

    @BeforeAll
    public static void suiteSetup() {
        log.info("=== Suite Setup: Health check + Auth token fetch ===");

        // 1. Health check
        Response health = BaseRequestSpec.getBaseSpec()
                .when()
                .get("/ping")
                .then()
                .extract().response();

        assertTrue(
                health.statusCode() == 201,
                "Health check failed — is Restful Booker running? Status: " + health.statusCode()
        );
        log.info("Health check passed — status {}", health.statusCode());

        // 2. Fetch auth token
        String body = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                ConfigManager.get().adminUsername(),
                ConfigManager.get().adminPassword()
        );

        Response authResponse = BaseRequestSpec.getBaseSpec()
                .body(body)
                .when()
                .post("/auth")
                .then()
                .extract().response();

        // Restful Booker quirk: wrong credentials return 200 with {"reason":"Bad credentials"}
        // So we check the token field exists, not just status code
        authToken = authResponse.jsonPath().getString("token");

        assertTrue(
                authToken != null && !authToken.isBlank(),
                "Auth token is null or empty — check admin credentials in config.properties"
        );
        log.info("Auth token obtained successfully");
    }

    // ── @Before ───────────────────────────────────────────────────────────────
    // Runs before each scenario EXCEPT @auth and @create tagged ones.
    // Creates a fresh booking and stores the ID in ScenarioContext.

    @Before(value = "not @auth and not @create", order = 10)
    public void createBooking(Scenario scenario) {
        log.info("[@Before] Creating booking for scenario: {}", scenario.getName());

        String payload = BookingPayloadBuilder.buildDefault();
        context.setLastRequestPayload(payload);

        Response response = BaseRequestSpec.getAuthSpec(authToken)
                .contentType("application/json")
                .accept("application/json")
                .body(payload)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        assertEquals(response.statusCode(), 200,
                "Failed to create booking in @Before hook");

        int id = response.jsonPath().getInt("bookingid");
        context.setBookingId(id);

        log.info("[@Before] Booking created — ID: {}", id);

        // SLA check on setup call
        long responseTime = response.getTime();
        assertTrue(
                responseTime <= ConfigManager.get().slaResponseTimeMs(),
                String.format("@Before booking creation exceeded SLA: %dms > %dms",
                        responseTime, ConfigManager.get().slaResponseTimeMs())
        );
    }

    // ── @After ────────────────────────────────────────────────────────────────
    // Runs after each scenario EXCEPT @auth tagged ones.
    // Deletes the booking created in @Before. Restful Booker returns 201 on DELETE.

    @After(value = "not @auth", order = 10)
    public void deleteBooking(Scenario scenario) {
        if (!context.hasBookingId()) {
            log.warn("[@After] No bookingId in context — skipping DELETE");
            return;
        }

        log.info("[@After] Deleting booking ID: {} for scenario: {}",
                context.getBookingId(), scenario.getName());

        Response response = BaseRequestSpec.getAuthSpec(authToken)
                .when()
                .delete("/booking/" + context.getBookingId())
                .then()
                .extract().response();

        // Restful Booker quirk: DELETE returns 201, not 204
        assertEquals(response.statusCode(), 201,
                "DELETE booking failed for ID: " + context.getBookingId());

        log.info("[@After] Booking {} deleted — status {}", context.getBookingId(), response.statusCode());
    }
}