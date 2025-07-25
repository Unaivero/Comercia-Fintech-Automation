@API @Smoke
Feature: Payment Status Check via API
  As a system or merchant
  I want to check the status of a payment transaction using an API
  So that I can confirm the payment outcome programmatically

  @Positive @API
  Scenario: Check status of a successful payment transaction
    Given a payment transaction with ID "txn_12345success" has been processed
    When I send a GET request to the "/status/txn_12345success" endpoint
    Then the API response status code should be 200
    And the API response should indicate the payment status is "Captured" or "Authorized"

  @Negative @API
  Scenario: Check status of a failed payment transaction
    Given a payment transaction with ID "txn_12345failed" has been processed
    When I send a GET request to the "/status/txn_12345failed" endpoint
    Then the API response status code should be 404
    And the API response should indicate an error message
