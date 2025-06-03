package com.comercia.fintech.base;

import com.comercia.fintech.utils.ConfigReader;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.junit.After;
import org.junit.Before;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase base que contiene configuración común para todas las pruebas
 */
public class BaseTest {
    
    protected static final String TIMESTAMP = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    
    @Before
    public void baseSetUp() {
        System.out.println("=== Starting test execution at: " + LocalDateTime.now() + " ===");
        loadConfiguration();
    }
    
    @After
    public void baseTearDown() {
        System.out.println("=== Test execution completed at: " + LocalDateTime.now() + " ===");
    }
    
    private void loadConfiguration() {
        // Ensure configuration is loaded
        String environment = ConfigReader.getProperty("environment", "test");
        System.out.println("Running tests in environment: " + environment);
    }
    
    /**
     * Toma una captura de pantalla para reportes de Allure
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] takeScreenshot(WebDriver driver) {
        if (driver != null) {
            try {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (Exception e) {
                System.err.println("Failed to take screenshot: " + e.getMessage());
            }
        }
        return new byte[0];
    }
    
    /**
     * Adjunta logs de texto a los reportes de Allure
     */
    @Attachment(value = "{0}", type = "text/plain")
    public String attachLog(String name, String content) {
        return content;
    }
    
    /**
     * Utility method para esperar un tiempo específico
     */
    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during wait", e);
        }
    }
}
