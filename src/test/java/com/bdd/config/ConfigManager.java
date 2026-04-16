package com.bdd.config;

import org.aeonbits.owner.ConfigFactory;

public class ConfigManager {

    private static final FrameworkConfig CONFIG =
            ConfigFactory.create(FrameworkConfig.class, System.getProperties(), System.getenv());

    // Singleton — no instantiation
    private ConfigManager() {}

    public static FrameworkConfig get() {
        return CONFIG;
    }
}