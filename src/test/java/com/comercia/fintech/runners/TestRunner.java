package com.comercia.fintech.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features", // Path to your feature files
    glue = "com.comercia.fintech.stepDefinitions", // Package where step definitions are located
    plugin = {
        "pretty", // Prints Gherkin steps to console for better readability
        "html:target/cucumber-reports/cucumber-pretty.html", // Generates an HTML report
        "json:target/cucumber-reports/CucumberTestReport.json", // Generates a JSON report
        "junit:target/cucumber-reports/CucumberTestReport.xml", // Generates a JUnit XML report
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" // Generates Allure results
    },
    monochrome = true, // Readable console output
    // tags = "@UI or @API" // Example: run scenarios with specific tags
    // tags = "@Smoke and not @WIP" // Example: run smoke tests but not work-in-progress
    publish = false // Set to true to publish reports to Cucumber Reports service (requires setup)
)
public class TestRunner {
    // This class will be empty. It's used to configure and run Cucumber tests.
}
