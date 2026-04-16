package com.bdd.config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:config/config.properties"
})
public interface FrameworkConfig extends Config {

    @Key("base.url")
    @DefaultValue("http://localhost:3001")
    String baseUrl();

    @Key("admin.username")
    @DefaultValue("admin")
    String adminUsername();

    @Key("admin.password")
    @DefaultValue("password123")
    String adminPassword();

    @Key("sla.response.time.ms")
    @DefaultValue("500")
    int slaResponseTimeMs();

    @Key("env")
    @DefaultValue("local")
    String env();

    @Key("allure.results.dir")
    @DefaultValue("target/allure-results")
    String allureResultsDir();
}