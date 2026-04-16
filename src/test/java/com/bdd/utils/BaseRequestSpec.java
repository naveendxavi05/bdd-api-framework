package com.bdd.utils;

import com.bdd.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class BaseRequestSpec {

    private static final RequestSpecification BASE_SPEC;

    static {
        BASE_SPEC = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get().baseUrl())
                .setContentType("application/json")
                .setAccept("application/json")
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    private BaseRequestSpec() {}

    /**
     * Unauthenticated base spec — for auth and read endpoints.
     */
    public static RequestSpecification getBaseSpec() {
        return RestAssured.given().spec(BASE_SPEC);
    }

    /**
     * Authenticated spec — injects token cookie.
     */
    public static RequestSpecification getAuthSpec(String token) {
        return RestAssured.given()
                .spec(BASE_SPEC)
                .cookie("token", token);
    }
}