package com.comercia.fintech.pageObjects;

import com.comercia.fintech.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import java.time.Duration;
import java.util.regex.Pattern;

public class CheckoutPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    // Regular expressions for validation
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{16}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");

    // --- Web Elements (using @FindBy) ---
    @FindBy(id = "cardNumber")
    private WebElement cardNumberInput;

    @FindBy(id = "cardHolderName")
    private WebElement cardHolderNameInput;

    @FindBy(id = "expiryMonth")
    private WebElement expiryMonthSelect;

    @FindBy(id = "expiryYear")
    private WebElement expiryYearSelect;

    @FindBy(id = "cvv")
    private WebElement cvvInput;

    @FindBy(id = "payButton")
    private WebElement payButton;

    @FindBy(id = "paymentMessage")
    private WebElement paymentMessageText;

    // --- Constructor ---
    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        int explicitWait = ConfigReader.getIntProperty("explicit.wait.seconds", 20);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        PageFactory.initElements(driver, this);
    }

    // --- Page Actions with Enhanced Validations ---
    public void navigateTo(String url) {
        System.out.println("Navigating to: " + url);
        try {
            driver.get(url);
            
            // Wait for page to load and verify key elements are present
            waitForElementToBeVisible(cardNumberInput, "Card number input field");
            
            // Validate page title
            String pageTitle = driver.getTitle();
            if (!pageTitle.contains("Dummy Checkout")) {
                throw new RuntimeException("Page title validation failed. Expected to contain 'Dummy Checkout', actual: " + pageTitle);
            }
            
            System.out.println("Page loaded successfully with title: " + pageTitle);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to checkout page: " + url, e);
        }
    }

    public void enterCardNumber(String cardNumber) {
        System.out.println("Entering card number: " + maskCardNumber(cardNumber));
        
        // Input validation
        validateCardNumberFormat(cardNumber);
        
        waitForElementToBeClickable(cardNumberInput, "Card number input");
        clearAndEnterText(cardNumberInput, cardNumber);
        validateCardNumberInput(cardNumber);
        
        System.out.println("Card number entered and validated successfully");
    }

    public void enterCardHolderName(String name) {
        System.out.println("Entering cardholder name: " + name);
        
        // Input validation
        validateCardholderNameFormat(name);
        
        waitForElementToBeClickable(cardHolderNameInput, "Cardholder name input");
        clearAndEnterText(cardHolderNameInput, name);
        validateTextInput(cardHolderNameInput, name, "Cardholder name");
        
        System.out.println("Cardholder name entered and validated successfully");
    }

    public void enterCvv(String cvv) {
        System.out.println("Entering CVV: ***");
        
        // Input validation
        validateCvvFormat(cvv);
        
        waitForElementToBeClickable(cvvInput, "CVV input");
        clearAndEnterText(cvvInput, cvv);
        validateCvvInput(cvv);
        
        System.out.println("CVV entered and validated successfully");
    }

    public void selectExpiryDate(String month, String year) {
        System.out.println("Selecting expiry: " + month + "/" + year);
        
        // Input validation
        validateExpiryDateFormat(month, year);
        
        waitForElementToBeClickable(expiryMonthSelect, "Expiry month select");
        waitForElementToBeClickable(expiryYearSelect, "Expiry year select");
        
        Select monthSelect = new Select(expiryMonthSelect);
        Select yearSelect = new Select(expiryYearSelect);
        
        // Validate options exist before selecting
        validateSelectOptionExists(monthSelect, month, "month");
        validateSelectOptionExists(yearSelect, year, "year");
        
        monthSelect.selectByValue(month);
        yearSelect.selectByValue(year);
        
        // Validate selections
        validateSelectValue(monthSelect, month, "Expiry month");
        validateSelectValue(yearSelect, year, "Expiry year");
        
        System.out.println("Expiry date selected and validated successfully");
    }

    public void clickPayButton() {
        System.out.println("Clicking Pay Now button");
        
        waitForElementToBeClickable(payButton, "Pay button");
        
        // Verify button is enabled
        if (!payButton.isEnabled()) {
            throw new RuntimeException("Pay button is disabled");
        }
        
        // Scroll element into view if needed
        scrollToElement(payButton);
        payButton.click();
        
        System.out.println("Pay button clicked successfully");
        
        // Wait for payment processing to complete
        waitForPaymentResult();
    }

    public String getPaymentResultMessage() {
        System.out.println("Getting payment result message from UI element...");
        
        try {
            // Wait for payment message to appear
            waitForElementToBeVisible(paymentMessageText, "Payment result message");
            
            String message = paymentMessageText.getText().trim();
            System.out.println("Payment result message: '" + message + "'");
            
            if (message.isEmpty()) {
                throw new RuntimeException("Payment result message is empty");
            }
            
            // Validate message format
            validatePaymentMessage(message);
            
            return message;
            
        } catch (TimeoutException e) {
            throw new RuntimeException("Payment result message did not appear within timeout", e);
        }
    }

    // --- Enhanced validation methods ---
    private void validateCardNumberFormat(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be empty");
        }
        
        String cleanCardNumber = cardNumber.replaceAll("\\s", "");
        if (!CARD_NUMBER_PATTERN.matcher(cleanCardNumber).matches()) {
            throw new IllegalArgumentException("Invalid card number format. Expected 16 digits, got: " + maskCardNumber(cardNumber));
        }
    }
    
    private void validateCardholderNameFormat(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cardholder name cannot be empty");
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid cardholder name format. Only letters and spaces allowed, 2-50 characters");
        }
    }
    
    private void validateCvvFormat(String cvv) {
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new IllegalArgumentException("CVV cannot be empty");
        }
        
        if (!CVV_PATTERN.matcher(cvv).matches()) {
            throw new IllegalArgumentException("Invalid CVV format. Expected 3-4 digits");
        }
    }
    
    private void validateExpiryDateFormat(String month, String year) {
        if (month == null || month.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry month cannot be empty");
        }
        
        if (year == null || year.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry year cannot be empty");
        }
        
        try {
            int monthInt = Integer.parseInt(month);
            int yearInt = Integer.parseInt(year);
            
            if (monthInt < 1 || monthInt > 12) {
                throw new IllegalArgumentException("Invalid month. Must be between 01-12");
            }
            
            int currentYear = java.time.Year.now().getValue();
            if (yearInt < currentYear || yearInt > currentYear + 20) {
                throw new IllegalArgumentException("Invalid year. Must be between " + currentYear + " and " + (currentYear + 20));
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Month and year must be numeric values");
        }
    }
    
    private void validateSelectOptionExists(Select selectElement, String value, String fieldName) {
        boolean optionExists = selectElement.getOptions().stream()
            .anyMatch(option -> option.getAttribute("value").equals(value));
            
        if (!optionExists) {
            throw new RuntimeException("Option '" + value + "' does not exist in " + fieldName + " dropdown");
        }
    }
    
    private void validatePaymentMessage(String message) {
        if (!message.startsWith("Payment Successful") && !message.startsWith("Error:")) {
            System.out.println("Warning: Unexpected payment message format: " + message);
        }
    }

    // --- Helper methods for specific card types ---
    public void enterValidCardDetails() {
        System.out.println("Entering details for a valid card");
        String validCard = ConfigReader.getProperty("test.card.valid", "4000000000000000");
        enterCardNumber(validCard);
    }

    public void enterExpiredCardDetails() {
        System.out.println("Entering details for an expired card");
        String expiredCard = ConfigReader.getProperty("test.card.expired", "4111111111111111");
        enterCardNumber(expiredCard);
    }

    public void enterValidExpiryDate() {
        System.out.println("Entering a valid expiry date");
        String validMonth = ConfigReader.getProperty("test.expiry.valid.month", "12");
        String validYear = ConfigReader.getProperty("test.expiry.valid.year", "2028");
        selectExpiryDate(validMonth, validYear);
    }

    public void enterExpiredExpiryDate() {
        System.out.println("Entering an expired expiry date");
        String expiredMonth = ConfigReader.getProperty("test.expiry.expired.month", "01");
        String expiredYear = ConfigReader.getProperty("test.expiry.expired.year", "2020");
        selectExpiryDate(expiredMonth, expiredYear);
    }

    // --- Private utility methods ---
    private void waitForElementToBeVisible(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element not visible within timeout: " + elementName, e);
        }
    }

    private void waitForElementToBeClickable(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element not clickable within timeout: " + elementName, e);
        }
    }

    private void clearAndEnterText(WebElement element, String text) {
        try {
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enter text: " + text, e);
        }
    }

    private void scrollToElement(WebElement element) {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", element);
            Thread.sleep(500); // Brief pause after scrolling
        } catch (Exception e) {
            System.err.println("Failed to scroll to element: " + e.getMessage());
        }
    }

    private void waitForPaymentResult() {
        try {
            // Wait for either success or error message to appear
            wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElement(paymentMessageText, "Payment Successful"),
                ExpectedConditions.textToBePresentInElement(paymentMessageText, "Error:")
            ));
        } catch (TimeoutException e) {
            System.err.println("Payment result did not appear within timeout: " + e.getMessage());
            throw new RuntimeException("Payment processing timeout", e);
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private void validateCardNumberInput(String expectedCardNumber) {
        String actualValue = cardNumberInput.getAttribute("value");
        if (!expectedCardNumber.equals(actualValue)) {
            throw new RuntimeException("Card number validation failed. Expected: " + 
                maskCardNumber(expectedCardNumber) + ", Actual: " + maskCardNumber(actualValue));
        }
    }

    private void validateTextInput(WebElement element, String expectedValue, String fieldName) {
        String actualValue = element.getAttribute("value");
        if (!expectedValue.equals(actualValue)) {
            throw new RuntimeException(fieldName + " validation failed. Expected: " + 
                expectedValue + ", Actual: " + actualValue);
        }
    }

    private void validateCvvInput(String expectedCvv) {
        String actualValue = cvvInput.getAttribute("value");
        if (!expectedCvv.equals(actualValue)) {
            throw new RuntimeException("CVV validation failed");
        }
    }

    private void validateSelectValue(Select selectElement, String expectedValue, String fieldName) {
        String actualValue = selectElement.getFirstSelectedOption().getAttribute("value");
        if (!expectedValue.equals(actualValue)) {
            throw new RuntimeException(fieldName + " validation failed. Expected: " + 
                expectedValue + ", Actual: " + actualValue);
        }
    }
}
