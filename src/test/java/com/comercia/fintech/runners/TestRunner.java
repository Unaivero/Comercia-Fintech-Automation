package com.comercia.fintech.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Test Runner principal para ejecutar todas las pruebas
 * Configuración mejorada con mejores reportes y manejo de errores
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.comercia.fintech.stepDefinitions",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber-pretty.html",
        "json:target/cucumber-reports/CucumberTestReport.json",
        "junit:target/cucumber-reports/CucumberTestReport.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        "timeline:target/cucumber-reports/timeline"
    },
    monochrome = true,
    tags = "@Smoke",
    publish = false,
    dryRun = false,
    snippets = io.cucumber.junit.CucumberOptions.SnippetType.CAMELCASE
)
public class TestRunner {
    
    /**
     * Este runner ejecuta todas las pruebas marcadas con @Smoke
     * 
     * Para ejecutar diferentes conjuntos de pruebas, use:
     * mvn test -Dcucumber.filter.tags="@UI"
     * mvn test -Dcucumber.filter.tags="@API" 
     * mvn test -Dcucumber.filter.tags="@Positive"
     * mvn test -Dcucumber.filter.tags="@Negative"
     * mvn test -Dcucumber.filter.tags="@UI and @Positive"
     */
}
