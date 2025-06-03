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
        } catch (IOException ex) {
            System.err.println("Error loading config properties file: " + CONFIG_FILE_PATH);
            ex.printStackTrace();
            // Consider throwing a runtime exception here if config is critical
            // throw new RuntimeException("Could not load config.properties", ex);
        }
    }

    /**
     * Gets a property value from the config.properties file.
     * @param key The property key.
     * @return The property value, or null if the key is not found or an error occurred.
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.err.println("Property not found for key: " + key + " in " + CONFIG_FILE_PATH);
        }
        return value;
    }

    /**
     * Gets a property value from the config.properties file.
     * If the key is not found, returns the specified default value.
     * @param key The property key.
     * @param defaultValue The value to return if the key is not found.
     * @return The property value, or the defaultValue if the key is not found.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // Example of how to get an integer property (add more types as needed)
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing integer for key: " + key + ", value: " + value + ". Returning default.");
            }
        }
        return defaultValue;
    }
}
