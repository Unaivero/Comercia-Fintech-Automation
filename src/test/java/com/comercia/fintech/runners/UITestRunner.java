package com.comercia.fintech.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner específico para pruebas de UI
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.comercia.fintech.stepDefinitions",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/ui-tests.html",
        "json:target/cucumber-reports/ui-tests.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@UI",
    monochrome = true
)
public class UITestRunner {
    /**
     * Ejecutar con: mvn test -Dtest=UITestRunner
     */
}
