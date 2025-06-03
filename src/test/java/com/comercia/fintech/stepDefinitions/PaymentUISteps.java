package com.comercia.fintech.stepDefinitions;

import com.comercia.fintech.pageObjects.CheckoutPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

public class PaymentUISteps {

    private WebDriver driver;
    private CheckoutPage checkoutPage;

    @Before("@UI") // This hook will run before scenarios tagged with @UI
    public void setUp() {
        System.out.println("Setting up WebDriver...");
        WebDriverManager.chromedriver().setup(); // Automatically downloads and sets up ChromeDriver
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Optional: run in headless mode
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        checkoutPage = new CheckoutPage(driver);
        System.out.println("WebDriver setup complete.");
    }

    @Given("I am on the merchant checkout page")
    public void i_am_on_the_merchant_checkout_page() {
        // For now, we'll use a dummy HTML page or a public demo site.
        // In a real scenario, this would be the URL of the application under test.
        // Navigate to the local dummy HTML file.
        String dummyPagePath = System.getProperty("user.dir") + "/src/test/resources/test_pages/dummy_checkout.html";
        // Ensure correct file URL format for different OS if necessary, but this usually works:
        String dummyPageUrl = "file:///" + dummyPagePath.replace("\\", "/"); 
        System.out.println("Navigating to dummy checkout page: " + dummyPageUrl);
        checkoutPage.navigateTo(dummyPageUrl);
    }

    @When("I enter valid card details")
    public void i_enter_valid_card_details() {
        checkoutPage.enterValidCardDetails();
    }

    @When("I enter {string} as the cardholder name")
    public void i_enter_as_the_cardholder_name(String cardholderName) {
        checkoutPage.enterCardHolderName(cardholderName);
    }

    @When("I enter {string} as the CVV")
    public void i_enter_as_the_cvv(String cvv) {
        checkoutPage.enterCvv(cvv);
    }

    @When("I enter a valid expiry date")
    public void i_enter_a_valid_expiry_date() {
        checkoutPage.enterValidExpiryDate();
    }

    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) {
        // Assuming buttonText helps identify the button if there are multiple
        // For now, we assume it's the main payment button.
        System.out.println("Clicking button: " + buttonText);
        checkoutPage.clickPayButton();
    }

    @Then("I should see a {string} message")
    public void i_should_see_a_message(String expectedMessage) {
        System.out.println("Verifying message: " + expectedMessage);
        String actualMessage = checkoutPage.getPaymentResultMessage();
        // The dummy_checkout.html now handles message display, so this should fetch the actual message.
        assertEquals(expectedMessage, actualMessage);
        System.out.println("Assertion passed: Expected '" + expectedMessage + "', got '" + actualMessage + "'");
    }

    @When("I enter card details for an expired card")
    public void i_enter_card_details_for_an_expired_card() {
        checkoutPage.enterExpiredCardDetails();
    }

    @When("I enter an expired expiry date")
    public void i_enter_an_expired_expiry_date() {
        checkoutPage.enterExpiredExpiryDate();
    }

    @After("@UI") // This hook will run after scenarios tagged with @UI
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing WebDriver...");
            driver.quit();
            System.out.println("WebDriver closed.");
        }
    }
}
