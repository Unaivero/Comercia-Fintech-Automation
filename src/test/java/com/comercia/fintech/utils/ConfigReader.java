package com.comercia.fintech.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_FILE_PATH = "src/test/resources/config.properties";

    static {
        properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(input);
            System.out.println("Config properties loaded successfully from: " + CONFIG_FILE_PATH);
            validateRequiredProperties();
        } catch (IOException ex) {
            System.err.println("Error loading config properties file: " + CONFIG_FILE_PATH);
            ex.printStackTrace();
            throw new RuntimeException("Could not load config.properties", ex);
        }
    }

    /**
     * Gets a property value from the config.properties file.
     * @param key The property key.
     * @return The property value, or null if the key is not found.
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.err.println("Property not found for key: " + key + " in " + CONFIG_FILE_PATH);
        }
        return value;
    }

    /**
     * Gets a property value with default fallback.
     * @param key The property key.
     * @param defaultValue The value to return if the key is not found.
     * @return The property value, or the defaultValue if the key is not found.
     */
    public static String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        if (value.equals(defaultValue)) {
            System.out.println("Using default value for key '" + key + "': " + defaultValue);
        }
        return value;
    }

    /**
     * Gets an integer property with validation.
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing integer for key: " + key + ", value: " + value + ". Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Gets a boolean property with validation.
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Gets a double property with validation.
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing double for key: " + key + ", value: " + value + ". Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Validates that required properties exist.
     */
    private static void validateRequiredProperties() {
        String[] requiredKeys = {
            "browser",
            "test.card.valid",
            "test.card.expired"
        };

        for (String key : requiredKeys) {
            if (getProperty(key) == null) {
                throw new RuntimeException("Required property missing: " + key);
            }
        }
    }

    /**
     * Gets environment-specific property.
     */
    public static String getEnvironmentProperty(String key) {
        String environment = getProperty("environment", "test");
        String envKey = environment + "." + key;
        String envValue = getProperty(envKey);
        
        // Fallback to non-environment specific if env-specific not found
        return envValue != null ? envValue : getProperty(key);
    }
}
