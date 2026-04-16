package com.bdd.steps;

import com.bdd.config.ConfigManager;
import com.bdd.utils.BaseRequestSpec;
import com.bdd.utils.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AuthSteps {

    private static final Logger log = LoggerFactory.getLogger(AuthSteps.class);
    private final ScenarioContext context;

    public AuthSteps(ScenarioContext context) {
        this.context = context;
    }

    @Given("the booking service is running")
    public void theBookingServiceIsRunning() {
        Response response = BaseRequestSpec.getBaseSpec()
                .when()
                .get("/ping")
                .then()
                .extract().response();

        assertEquals(response.statusCode(), 201, "Service health check failed");
        log.info("Booking service is running — ping returned 201");
    }

    @When("I request a token with valid admin credentials")
    public void iRequestTokenWithValidCredentials() {
        String body = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                ConfigManager.get().adminUsername(),
                ConfigManager.get().adminPassword()
        );

        Response response = BaseRequestSpec.getBaseSpec()
                .body(body)
                .when()
                .post("/auth")
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @When("I request a token with invalid credentials")
    public void iRequestTokenWithInvalidCredentials() {
        String body = "{\"username\":\"wrong\",\"password\":\"wrong\"}";

        Response response = BaseRequestSpec.getBaseSpec()
                .body(body)
                .when()
                .post("/auth")
                .then()
                .extract().response();

        context.setLastResponse(response);
        assertSla(response);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertEquals(context.getLastResponse().statusCode(), expectedStatus,
                "Unexpected status code");
    }

    @And("the response should contain a valid token")
    public void theResponseShouldContainAValidToken() {
        String token = context.getLastResponse().jsonPath().getString("token");
        assertTrue(token != null && !token.isBlank(), "Token is null or blank");
        log.info("Valid token received");
    }

    @And("the response should contain {string}")
    public void theResponseShouldContain(String expectedText) {
        String body = context.getLastResponse().getBody().asString();
        assertTrue(body.contains(expectedText),
                "Response body does not contain: " + expectedText);
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