@auth @smoke
Feature: Authentication API
  As an admin user
  I want to generate an auth token
  So that I can perform secured booking operations

  Scenario: Generate token with valid credentials
    Given the booking service is running
    When I request a token with valid admin credentials
    Then the response status should be 200
    And the response should contain a valid token

  Scenario: Generate token with invalid credentials
    Given the booking service is running
    When I request a token with invalid credentials
    Then the response status should be 200
    And the response should contain "Bad credentials"