Feature: Merchant Payment Checkout UI Simulation
  As a customer
  I want to make a payment through the merchant checkout page
  So that I can purchase goods or services

  Background:
    Given I am on the merchant checkout page

  Scenario: Successful Payment
    When I enter valid card details
    And I enter "John Doe" as the cardholder name
    And I enter "123" as the CVV
    And I enter a valid expiry date
    And I click the "Pay Now" button
    Then I should see a "Payment Successful" message

  Scenario: Expired Card Payment
    When I enter card details for an expired card
    And I enter "Jane Smith" as the cardholder name
    And I enter "456" as the CVV
    And I enter an expired expiry date
    And I click the "Pay Now" button
    Then I should see an "Error: Card Expired" message
