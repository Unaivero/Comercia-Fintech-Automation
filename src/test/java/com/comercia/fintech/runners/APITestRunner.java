package com.comercia.fintech.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner específico para pruebas de API
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.comercia.fintech.stepDefinitions",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/api-tests.html",
        "json:target/cucumber-reports/api-tests.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@API",
    monochrome = true
)
public class APITestRunner {
    /**
     * Ejecutar con: mvn test -Dtest=APITestRunner
     */
}
