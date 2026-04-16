package com.bdd.utils;

import com.github.javafaker.Faker;

public class BookingPayloadBuilder {

    private static final Faker faker = new Faker();

    private BookingPayloadBuilder() {}

    /**
     * Builds a valid default booking JSON payload using JavaFaker.
     * The FAKER sentinel: if Faker generates null/blank, we substitute
     * a hardcoded fallback so tests never fail due to empty Faker output.
     */
    public static String buildDefault() {
        String firstName = fallback(faker.name().firstName(), "John");
        String lastName  = fallback(faker.name().lastName(),  "Doe");
        int    totalPrice = faker.number().numberBetween(100, 1000);
        String checkIn   = "2025-01-01";
        String checkOut  = "2025-01-07";
        String addNeeds  = fallback(faker.food().dish(), "None");

        return String.format("""
                {
                  "firstname": "%s",
                  "lastname": "%s",
                  "totalprice": %d,
                  "depositpaid": true,
                  "bookingdates": {
                    "checkin": "%s",
                    "checkout": "%s"
                  },
                  "additionalneeds": "%s"
                }
                """, firstName, lastName, totalPrice, checkIn, checkOut, addNeeds);
    }

    /**
     * Builds a booking with explicit values — used in Scenario Outline steps.
     */
    public static String buildWith(String firstName, String lastName,
                                   int totalPrice, boolean depositPaid,
                                   String checkIn, String checkOut,
                                   String additionalNeeds) {
        return String.format("""
                {
                  "firstname": "%s",
                  "lastname": "%s",
                  "totalprice": %d,
                  "depositpaid": %b,
                  "bookingdates": {
                    "checkin": "%s",
                    "checkout": "%s"
                  },
                  "additionalneeds": "%s"
                }
                """, firstName, lastName, totalPrice, depositPaid,
                checkIn, checkOut, additionalNeeds);
    }

    private static String fallback(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}