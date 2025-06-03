package com.comercia.fintech.pageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class CheckoutPage {
    private WebDriver driver;

    // --- Web Elements (using @FindBy) ---
    @FindBy(id = "cardNumber") // Example ID, replace with actual
    private WebElement cardNumberInput;

    @FindBy(id = "cardHolderName") // Example ID, replace with actual
    private WebElement cardHolderNameInput;

    @FindBy(id = "expiryMonth") // Example ID, replace with actual
    private WebElement expiryMonthSelect;

    @FindBy(id = "expiryYear") // Example ID, replace with actual
    private WebElement expiryYearSelect;

    @FindBy(id = "cvv") // Example ID, replace with actual
    private WebElement cvvInput;

    @FindBy(id = "payButton") // Example ID, replace with actual
    private WebElement payButton;

    @FindBy(id = "paymentMessage") // Example ID, replace with actual
    private WebElement paymentMessageText;

    // --- Constructor ---
    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // --- Page Actions ---
    public void navigateTo(String url) {
        System.out.println("Navigating to: " + url);
        driver.get(url);
    }

    public void enterCardNumber(String cardNumber) {
        System.out.println("Entering card number: " + cardNumber);
        cardNumberInput.sendKeys(cardNumber);
    }

    public void enterCardHolderName(String name) {
        System.out.println("Entering cardholder name: " + name);
        cardHolderNameInput.sendKeys(name);
    }

    public void enterCvv(String cvv) {
        System.out.println("Entering CVV: " + cvv);
        cvvInput.sendKeys(cvv);
    }

    public void selectExpiryDate(String month, String year) {
        System.out.println("Selecting expiry: " + month + "/" + year);
        new Select(expiryMonthSelect).selectByValue(month); // Using selectByValue as dummy HTML has numeric values
        new Select(expiryYearSelect).selectByValue(year);
    }

    public void clickPayButton() {
        System.out.println("Clicking Pay Now button");
        payButton.click();
    }

    public String getPaymentResultMessage() {
        System.out.println("Getting payment result message from UI element...");
        // It might take a moment for the message to appear, an explicit wait could be good here in a real scenario
        // For simplicity now, we assume it's present after the click action completes.
        String message = paymentMessageText.getText();
        System.out.println("Raw message from UI: '" + message + "'");
        return message;
    }

    // --- Helper methods for specific card types (placeholders) ---
    public void enterValidCardDetails() {
        System.out.println("Entering details for a valid card (placeholder)");
        enterCardNumber("4000000000000000"); // Dummy valid card
    }

    public void enterExpiredCardDetails() {
        System.out.println("Entering details for an expired card (placeholder)");
        enterCardNumber("4111111111111111"); // Dummy expired card
    }

    public void enterValidExpiryDate() {
        System.out.println("Entering a valid expiry date (placeholder) e.g., 12/2028");
        selectExpiryDate("12", "2028");
    }

    public void enterExpiredExpiryDate() {
        System.out.println("Entering an expired expiry date (placeholder) e.g., 01/2020");
        selectExpiryDate("01", "2020");
    }
}
