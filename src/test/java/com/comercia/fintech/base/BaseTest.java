package com.comercia.fintech.base;

import com.comercia.fintech.monitoring.TestObservabilitySystem;
import com.comercia.fintech.utils.ConfigReader;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.junit.After;
import org.junit.Before;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced Base Test with Enterprise Observability
 */
public class BaseTest {
    
    protected static final String TIMESTAMP = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    private LocalDateTime testStartTime;
    private String currentTestName;
    private String currentFeature;
    
    @Before
    public void baseSetUp() {
        testStartTime = LocalDateTime.now();
        currentTestName = getTestName();
        currentFeature = extractFeatureFromTest(currentTestName);
        
        System.out.println("=== Starting test execution at: " + testStartTime + " ===");
        
        // Initialize observability
        TestObservabilitySystem.recordTestStart(
            currentTestName, 
            currentFeature, 
            ConfigReader.getProperty("browser", "chrome"),
            ConfigReader.getProperty("environment", "test")
        );
        
        loadConfiguration();
    }
    
    @After
    public void baseTearDown() {
        LocalDateTime endTime = LocalDateTime.now();
        Duration testDuration = Duration.between(testStartTime, endTime);
        
        // Determine test result (simplified - in real implementation would check test status)
        String result = "PASSED"; // This should be determined from test execution context
        String errorMessage = null;
        
        // Record test completion
        TestObservabilitySystem.recordTestCompletion(
            currentTestName,
            currentFeature,
            result,
            testDuration,
            errorMessage
        );
        
        // Check for alerts
        TestObservabilitySystem.checkAlertConditions();
        
        System.out.println("=== Test execution completed at: " + endTime + " ===");
        System.out.println("=== Test duration: " + testDuration.toMillis() + "ms ===");
    }
    
    private void loadConfiguration() {
        String environment = ConfigReader.getProperty("environment", "test");
        System.out.println("Running tests in environment: " + environment);
        
        // Update Selenium Grid status if available
        updateGridStatus();
    }
    
    private void updateGridStatus() {
        try {
            // This would typically query the Selenium Grid API
            // For now, using mock values
            TestObservabilitySystem.updateSeleniumGridStatus(10, 8, 2);
        } catch (Exception e) {
            System.err.println("Failed to update grid status: " + e.getMessage());
        }
    }
    
    private String getTestName() {
        // Extract test name from stack trace or context
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getMethodName().startsWith("test") || 
                element.getClassName().contains("Test")) {
                return element.getClassName() + "." + element.getMethodName();
            }
        }
        return "unknown_test";
    }
    
    private String extractFeatureFromTest(String testName) {
        if (testName.toLowerCase().contains("payment")) return "payment";
        if (testName.toLowerCase().contains("auth")) return "authentication";
        if (testName.toLowerCase().contains("checkout")) return "checkout";
        if (testName.toLowerCase().contains("api")) return "api";
        if (testName.toLowerCase().contains("ui")) return "ui";
        return "general";
    }
    
    /**
     * Enhanced screenshot capture with observability
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] takeScreenshot(WebDriver driver) {
        if (driver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                
                // Record UI performance metric
                TestObservabilitySystem.recordPageLoad(
                    driver.getCurrentUrl(), 
                    Duration.ofMillis(100), // Mock page load time
                    true
                );
                
                return screenshot;
            } catch (Exception e) {
                System.err.println("Failed to take screenshot: " + e.getMessage());
                TestObservabilitySystem.recordPageLoad(
                    "unknown_page", 
                    Duration.ofMillis(0), 
                    false
                );
            }
        }
        return new byte[0];
    }
    
    /**
     * Enhanced logging with observability
     */
    @Attachment(value = "{0}", type = "text/plain")
    public String attachLog(String name, String content) {
        return content;
    }
    
    /**
     * Record API call performance
     */
    protected void recordApiCall(String endpoint, String method, int statusCode, 
                                long responseTime, long responseSize) {
        TestObservabilitySystem.recordApiResponse(
            endpoint, method, statusCode, responseTime, responseSize
        );
    }
    
    /**
     * Record business transaction
     */
    protected void recordBusinessTransaction(String type, String status, 
                                           double amount, String currency) {
        TestObservabilitySystem.recordBusinessTransaction(type, status, amount, currency);
    }
    
    /**
     * Get real-time dashboard data
     */
    protected void printDashboardStatus() {
        var dashboard = TestObservabilitySystem.getRealTimeDashboard();
        System.out.println("=== Real-time Dashboard ===");
        dashboard.forEach((key, value) -> 
            System.out.println(key + ": " + value));
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
