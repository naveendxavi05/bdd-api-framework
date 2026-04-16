package com.bdd.utils;

import io.restassured.response.Response;

/**
 * PicoContainer injects one instance of this class per scenario.
 * Shared state between step definition classes — no static fields,
 * no ThreadLocal needed.
 */
public class ScenarioContext {

    // Booking ID created in @Before hook, deleted in @After hook
    private int bookingId = -1;

    // Last HTTP response — used for assertion steps
    private Response lastResponse;

    // Last request payload — attached to Allure report on failure
    private String lastRequestPayload;

    // ── bookingId ────────────────────────────────────────────────

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public boolean hasBookingId() {
        return bookingId > 0;
    }

    // ── lastResponse ─────────────────────────────────────────────

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    // ── lastRequestPayload ───────────────────────────────────────

    public String getLastRequestPayload() {
        return lastRequestPayload;
    }

    public void setLastRequestPayload(String lastRequestPayload) {
        this.lastRequestPayload = lastRequestPayload;
    }
}