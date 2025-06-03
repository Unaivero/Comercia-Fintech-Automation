package com.comercia.fintech.stepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.comercia.fintech.utils.ConfigReader;
import io.qameta.allure.Attachment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class PaymentAPISteps {

    private Response response;
    private String transactionId;
    private String baseUri;

    @Given("a payment transaction with ID {string} has been processed")
    public void a_payment_transaction_with_id_has_been_processed(String txnId) {
        this.transactionId = txnId;
        this.baseUri = ConfigReader.getProperty("api.base.uri", "https://api.example-fintech.com/payments");
        
        System.out.println("Simulating pre-existing transaction ID: " + txnId);
        System.out.println("Using API base URI: " + baseUri);
        
        // Log transaction setup for Allure report
        attachLog("Transaction Setup", "Transaction ID: " + txnId + "\nBase URI: " + baseUri);
    }

    @When("I send a GET request to the {string} endpoint")
    public void i_send_a_get_request_to_the_endpoint(String endpointPath) {
        try {
            RestAssured.baseURI = baseUri;
            
            // Set timeout from config
            int timeout = ConfigReader.getIntProperty("api.timeout.seconds", 30);
            
            RequestSpecification request = RestAssured.given()
                .config(RestAssured.config().httpClient(
                    org.apache.http.client.config.RequestConfig.custom()
                        .setSocketTimeout(timeout * 1000)
                        .setConnectTimeout(timeout * 1000)
                        .build()));

            // Replace placeholder {txnId} if present in the endpoint path
            String actualEndpoint = endpointPath.replace("{txnId}", this.transactionId);
            System.out.println("Sending GET request to: " + baseUri + actualEndpoint);

            // Mocking the API call as RestAssured would try to make a real HTTP request
            // In a real test, you would execute: response = request.get(actualEndpoint);
            
            if (this.transactionId.endsWith("success")) {
                response = RestAssured.given()
                    .when()
                    .get("https://run.mocky.io/v3/0199c398-9657-4578-9e07-f609093f0b0e");
                System.out.println("Mocked successful API response.");
            } else if (this.transactionId.endsWith("failed")) {
                response = RestAssured.given()
                    .when()
                    .get("https://run.mocky.io/v3/42042250-0126-4634-839c-90d512e62a0c");
                System.out.println("Mocked failed API response.");
            } else {
                response = RestAssured.given()
                    .when()
                    .get("https://run.mocky.io/v3/42042250-0126-4634-839c-90d512e62a0c");
                System.out.println("Mocked default API response.");
            }
            
            // Log response details
            attachApiResponse(response);
            
        } catch (Exception e) {
            System.err.println("Failed to send API request: " + e.getMessage());
            throw new RuntimeException("API request failed", e);
        }
    }

    @Then("the API response status code should be {int}")
    public void the_api_response_status_code_should_be(Integer expectedStatusCode) {
        try {
            assertNotNull("Response is null", response);
            
            int actualStatusCode = response.getStatusCode();
            System.out.println("Verifying API response status code. Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode);
            
            assertEquals("Status code mismatch", (int) expectedStatusCode, actualStatusCode);
            System.out.println("Status code assertion passed.");
            
        } catch (Exception e) {
            System.err.println("Status code verification failed: " + e.getMessage());
            throw new RuntimeException("Failed to verify status code", e);
        }
    }

    @Then("the API response should indicate the payment status is {string} or {string}")
    public void the_api_response_should_indicate_the_payment_status_is(String status1, String status2) {
        try {
            assertNotNull("Response is null", response);
            
            String responseBody = response.getBody().asString();
            System.out.println("Response body: " + responseBody);
            
            String paymentStatus = response.jsonPath().getString("data.status");
            System.out.println("Verifying payment status. Expected: '" + status1 + "' or '" + status2 + "', Actual: '" + paymentStatus + "'");
            
            boolean statusMatches = status1.equalsIgnoreCase(paymentStatus) || status2.equalsIgnoreCase(paymentStatus);
            assertTrue("Payment status does not match expected values. Expected: '" + status1 + "' or '" + status2 + "', Actual: '" + paymentStatus + "'", statusMatches);
            
            System.out.println("Payment status assertion passed.");
            
        } catch (Exception e) {
            System.err.println("Payment status verification failed: " + e.getMessage());
            throw new RuntimeException("Failed to verify payment status", e);
        }
    }

    @Then("the API response should indicate an error message")
    public void the_api_response_should_indicate_an_error_message() {
        try {
            assertNotNull("Response is null", response);
            
            String responseBody = response.getBody().asString();
            System.out.println("Response body: " + responseBody);
            
            // Check for error indicators in response
            boolean hasError = responseBody.contains("error") || 
                              responseBody.contains("Error") || 
                              responseBody.contains("failed") ||
                              response.getStatusCode() >= 400;
            
            assertTrue("Response should indicate an error", hasError);
            System.out.println("Error message verification passed.");
            
        } catch (Exception e) {
            System.err.println("Error message verification failed: " + e.getMessage());
            throw new RuntimeException("Failed to verify error message", e);
        }
    }

    // --- Utility methods for Allure reporting ---
    @Attachment(value = "API Response", type = "application/json")
    public String attachApiResponse(Response response) {
        if (response != null) {
            return "Status Code: " + response.getStatusCode() + "\n" +
                   "Response Time: " + response.getTime() + "ms\n" +
                   "Response Body: " + response.getBody().asString();
        }
        return "No response available";
    }

    @Attachment(value = "{0}", type = "text/plain")
    public String attachLog(String name, String content) {
        return content;
    }

    // --- Mock API helper methods ---
    public Response authorizePayment(String cardNumber, String expiry, String cvv, double amount) {
        try {
            RestAssured.baseURI = baseUri;
            RequestSpecification request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(String.format("{\"cardNumber\": \"%s\", \"expiry\": \"%s\", \"cvv\": \"%s\", \"amount\": %.2f}", 
                                    cardNumber, expiry, cvv, amount));
            
            System.out.println("Mocking POST /authorize");
            return RestAssured.given().when().get("https://run.mocky.io/v3/0199c398-9657-4578-9e07-f609093f0b0e");
            
        } catch (Exception e) {
            System.err.println("Failed to authorize payment: " + e.getMessage());
            throw new RuntimeException("Payment authorization failed", e);
        }
    }

    public Response capturePayment(String transactionId, double amount) {
        try {
            RestAssured.baseURI = baseUri;
            RequestSpecification request = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(String.format("{\"transactionId\": \"%s\", \"amount\": %.2f}", transactionId, amount));
            
            System.out.println("Mocking POST /capture");
            return RestAssured.given().when().get("https://run.mocky.io/v3/0199c398-9657-4578-9e07-f609093f0b0e");
            
        } catch (Exception e) {
            System.err.println("Failed to capture payment: " + e.getMessage());
            throw new RuntimeException("Payment capture failed", e);
        }
    }
}
