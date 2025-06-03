package com.comercia.fintech.stepDefinitions;

import com.comercia.fintech.pageObjects.CheckoutPage;
import com.comercia.fintech.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

public class PaymentUISteps {

    private WebDriver driver;
    private CheckoutPage checkoutPage;

    @Before("@UI")
    public void setUp() {
        System.out.println("Setting up WebDriver...");
        try {
            String browser = ConfigReader.getProperty("browser", "chrome").toLowerCase();
            boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless.execution", "false"));
            boolean maximize = Boolean.parseBoolean(ConfigReader.getProperty("browser.maximize", "true"));
            
            driver = createWebDriver(browser, headless);
            
            // Set timeouts from config
            int implicitWait = ConfigReader.getIntProperty("implicit.wait.seconds", 10);
            int pageLoadTimeout = ConfigReader.getIntProperty("page.load.timeout.seconds", 30);
            
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
            
            if (maximize) {
                driver.manage().window().maximize();
            }
            
            checkoutPage = new CheckoutPage(driver);
            System.out.println("WebDriver setup complete with browser: " + browser);
        } catch (Exception e) {
            System.err.println("Error setting up WebDriver: " + e.getMessage());
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    private WebDriver createWebDriver(String browser, boolean headless) {
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless");
                }
                chromeOptions.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
                return new ChromeDriver(chromeOptions);
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
                
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
                
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
    }

    @Given("I am on the merchant checkout page")
    public void i_am_on_the_merchant_checkout_page() {
        try {
            String checkoutUrl = ConfigReader.getProperty("ui.checkout.page.url");
            if (checkoutUrl == null) {
                // Fallback to dynamic path construction
                String dummyPagePath = System.getProperty("user.dir") + "/src/test/resources/test_pages/dummy_checkout.html";
                checkoutUrl = "file:///" + dummyPagePath.replace("\\", "/");
            }
            
            System.out.println("Navigating to checkout page: " + checkoutUrl);
            checkoutPage.navigateTo(checkoutUrl);
            
            // Verify page loaded correctly
            String pageTitle = driver.getTitle();
            assertTrue("Page did not load correctly", pageTitle.contains("Dummy Checkout"));
            
        } catch (Exception e) {
            takeScreenshot("navigation_failure");
            throw new RuntimeException("Failed to navigate to checkout page", e);
        }
    }

    @When("I enter valid card details")
    public void i_enter_valid_card_details() {
        try {
            String validCard = ConfigReader.getProperty("test.card.valid", "4000000000000000");
            checkoutPage.enterCardNumber(validCard);
        } catch (Exception e) {
            takeScreenshot("enter_card_details_failure");
            throw new RuntimeException("Failed to enter valid card details", e);
        }
    }

    @When("I enter {string} as the cardholder name")
    public void i_enter_as_the_cardholder_name(String cardholderName) {
        try {
            checkoutPage.enterCardHolderName(cardholderName);
        } catch (Exception e) {
            takeScreenshot("enter_cardholder_name_failure");
            throw new RuntimeException("Failed to enter cardholder name: " + cardholderName, e);
        }
    }

    @When("I enter {string} as the CVV")
    public void i_enter_as_the_cvv(String cvv) {
        try {
            checkoutPage.enterCvv(cvv);
        } catch (Exception e) {
            takeScreenshot("enter_cvv_failure");
            throw new RuntimeException("Failed to enter CVV", e);
        }
    }

    @When("I enter a valid expiry date")
    public void i_enter_a_valid_expiry_date() {
        try {
            String validMonth = ConfigReader.getProperty("test.expiry.valid.month", "12");
            String validYear = ConfigReader.getProperty("test.expiry.valid.year", "2028");
            checkoutPage.selectExpiryDate(validMonth, validYear);
        } catch (Exception e) {
            takeScreenshot("enter_expiry_date_failure");
            throw new RuntimeException("Failed to enter valid expiry date", e);
        }
    }

    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) {
        try {
            System.out.println("Clicking button: " + buttonText);
            checkoutPage.clickPayButton();
            
            // Wait a moment for the response to appear
            Thread.sleep(1000);
            
        } catch (Exception e) {
            takeScreenshot("click_button_failure");
            throw new RuntimeException("Failed to click button: " + buttonText, e);
        }
    }

    @Then("I should see a {string} message")
    public void i_should_see_a_message(String expectedMessage) {
        try {
            System.out.println("Verifying message: " + expectedMessage);
            String actualMessage = checkoutPage.getPaymentResultMessage();
            
            assertEquals("Payment result message does not match", expectedMessage, actualMessage);
            System.out.println("Assertion passed: Expected '" + expectedMessage + "', got '" + actualMessage + "'");
            
        } catch (Exception e) {
            takeScreenshot("message_verification_failure");
            throw new RuntimeException("Failed to verify message: " + expectedMessage, e);
        }
    }

    @When("I enter card details for an expired card")
    public void i_enter_card_details_for_an_expired_card() {
        try {
            String expiredCard = ConfigReader.getProperty("test.card.expired", "4111111111111111");
            checkoutPage.enterCardNumber(expiredCard);
        } catch (Exception e) {
            takeScreenshot("enter_expired_card_failure");
            throw new RuntimeException("Failed to enter expired card details", e);
        }
    }

    @When("I enter an expired expiry date")
    public void i_enter_an_expired_expiry_date() {
        try {
            String expiredMonth = ConfigReader.getProperty("test.expiry.expired.month", "01");
            String expiredYear = ConfigReader.getProperty("test.expiry.expired.year", "2020");
            checkoutPage.selectExpiryDate(expiredMonth, expiredYear);
        } catch (Exception e) {
            takeScreenshot("enter_expired_expiry_failure");
            throw new RuntimeException("Failed to enter expired expiry date", e);
        }
    }

    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] takeScreenshot(String name) {
        try {
            if (driver != null) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
        return new byte[0];
    }

    @After("@UI")
    public void tearDown() {
        try {
            if (driver != null) {
                System.out.println("Closing WebDriver...");
                driver.quit();
                System.out.println("WebDriver closed successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error closing WebDriver: " + e.getMessage());
        }
    }
}
