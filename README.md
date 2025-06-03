# Comercia Fintech Automation

## Project Purpose

This project is a Quality Assurance (QA) automation framework designed to simulate and test a Fintech payment processing flow, similar to services offered by companies like Comercia Global Payments. It demonstrates a comprehensive approach to testing by incorporating both UI (User Interface) and API (Application Programming Interface) level tests.

The framework is built using Java and leverages several industry-standard tools and design patterns:

- **Java**: Core programming language.
- **Cucumber + Gherkin**: For Behavior-Driven Development (BDD), allowing tests to be written in a human-readable format.
- **Selenium WebDriver**: For automating UI interactions, simulating a merchant checkout page experience.
- **RestAssured**: For testing backend APIs, simulating requests to endpoints like `/authorize`, `/capture`, and `/status/{txnId}`.
- **JUnit**: As the primary test runner for executing Cucumber features.
- **Maven**: As the build automation tool and for managing project dependencies.
- **Page Object Model (POM)**: Design pattern applied for UI tests to enhance maintainability and reduce code duplication.

## Project Structure

- `src/test/java/com/comercia/fintech/pageObjects`: Contains Page Object classes for UI elements and interactions.
- `src/test/java/com/comercia/fintech/stepDefinitions`: Contains Step Definition classes that map Gherkin steps to Java code.
- `src/test/java/com/comercia/fintech/runners`: Contains JUnit test runner classes to execute Cucumber features.
- `src/test/resources/features`: Contains `.feature` files written in Gherkin, describing test scenarios.
- `pom.xml`: Maven Project Object Model file, defining project dependencies and build configurations.
- `target/cucumber-reports/`: Directory where test execution reports (HTML, JSON, XML) will be generated.

## Prerequisites

- Java Development Kit (JDK) 11 or higher installed.
- Apache Maven installed.
- Google Chrome browser installed (as the current WebDriver setup uses ChromeDriver).
- ChromeDriver executable: Ensure it's in your system's PATH or update the path in `PaymentUISteps.java` (`System.setProperty("webdriver.chrome.driver", "path/to/your/chromedriver");`).
- **Allure command-line tool**: Required to generate and serve the Allure HTML report. Installation instructions can be found on the [official Allure Framework documentation](https://allurereport.org/docs/gettingstarted-installation/).

## How to Run Tests

1.  **Clone the repository or set up the project.**
2.  **Open a terminal or command prompt.**
3.  **Navigate to the project's root directory** (where `pom.xml` is located).
    ```bash
    cd /path/to/Comercia Fintech Automation
    ```
4.  **Clean and build the project using Maven:**
    ```bash
    mvn clean install
    ```
    This command will download all necessary dependencies and compile the project.

5.  **Run the tests using Maven:**
    ```bash
    mvn test
    ```
    This command will execute the tests defined in the `TestRunner.java` class.

## Viewing Reports

After the tests have run, several types of reports are generated:

### Standard Cucumber Reports

These reports are generated in the `target/cucumber-reports/` directory:
- **HTML Report**: `target/cucumber-reports/cucumber-pretty.html` (Open this file in a web browser for a user-friendly view of test results).
- **JSON Report**: `target/cucumber-reports/CucumberTestReport.json`
- **XML Report**: `target/cucumber-reports/CucumberTestReport.xml` (Useful for CI/CD integrations).

### Allure Reports

Allure provides a more detailed and interactive reporting experience.

1.  **Allure Results Generation**:
    When you run `mvn test`, Allure result files (JSON and other attachment files) are generated in the `target/allure-results/` directory.

2.  **Generating and Viewing the Allure HTML Report**:
    To generate and view the HTML report, you need the Allure command-line tool installed.
    Open your terminal, navigate to the project root directory, and run the following commands:

    *   To generate the report (output will be in `target/allure-report`):
        ```bash
        allure generate target/allure-results --clean -o target/allure-report
        ```
    *   To open/serve the generated report in your web browser:
        ```bash
        allure open target/allure-report
        ```
    This will start a local web server and open the Allure report dashboard.

## Notes

- The UI tests currently use placeholder navigation and element interactions. You will need to update `CheckoutPage.java` with actual web element locators and potentially a real URL or a local dummy HTML page for `checkoutPage.navigateTo()` in `PaymentUISteps.java`.
- The API tests use mock URIs from `https://run.mocky.io/` to simulate responses. The base URI in `PaymentAPISteps.java` (`https://api.example-fintech.com/payments`) is also a placeholder.
- Basic logging is done via `System.out.println`. For more advanced logging, consider integrating a dedicated logging framework like Log4j or SLF4J with a concrete binding.
