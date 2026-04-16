@booking @smoke
Feature: Booking API
  As an admin user
  I want to manage hotel bookings
  So that I can create, read, update and delete bookings

  Scenario: Get all bookings
    Given the booking service is running
    When I request all bookings
    Then the response status should be 200
    And the response should contain a list of bookings

  Scenario: Get booking by ID
    Given the booking service is running
    When I request the booking by ID
    Then the response status should be 200
    And the response should contain valid booking details

  Scenario: Update booking with full payload
    Given the booking service is running
    When I update the booking with new details
    Then the response status should be 200
    And the response should reflect the updated details

  Scenario: Partially update booking
    Given the booking service is running
    When I partially update the booking firstname to "James"
    Then the response status should be 200
    And the response firstname should be "James"

  Scenario: Delete booking
    Given the booking service is running
    When I delete the booking
    Then the response status should be 201

  @create
  Scenario Outline: Create booking with multiple data sets
    Given the booking service is running
    When I create a booking with firstname "<firstname>" lastname "<lastname>" price <price> deposit "<deposit>" checkin "<checkin>" checkout "<checkout>"
    Then the response status should be 200
    And the response should contain a valid booking ID

    Examples:
      | firstname | lastname | price | deposit | checkin    | checkout   |
      | James     | Brown    | 200   | true    | 2025-02-01 | 2025-02-07 |
      | Alice     | Smith    | 350   | false   | 2025-03-10 | 2025-03-15 |
      | Ravi      | Kumar    | 500   | true    | 2025-04-01 | 2025-04-10 |