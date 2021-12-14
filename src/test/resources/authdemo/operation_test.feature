Feature: Operation Tests
  Operation Tests for Credential System

  Scenario: Clear the store file
    Given the store file was deleted
    Then the result will be "true"

  Scenario: Create a User
    When I create a user named "spongebob" with password "KrabbyPatty"
    And I reset the credential cache
    When I verify the password "KrabbyPatty" for user "spongebob"
    Then the result will be "true"

  Scenario: Test A Bad Login
    Given user "spongebob" exists in the credential system.
    When I verify the password "MrKrabs" for user "spongebob" 
    Then the result will be "false"
    And the bad login count for user "spongebob" will be "non-zero".

  Scenario: Change Password
    Given user "spongebob" exists in the credential system.
    When I change the password for user "spongebob" to "ILoveSquidward"
    Then the result will be "true"

  Scenario: Change Password to invalid value for policy
    Given user "spongebob" exists in the credential system.
    When I change the password for user "spongebob" to "bob"
    Then the result will be "false"

  Scenario: Verify New Password
    Given user "spongebob" exists in the credential system.
    And I reset the credential cache
    When I verify the password "ILoveSquidward" for user "spongebob"
    Then the result will be "true"
  
  Scenario: Verify Old Password
    Given user "spongebob" exists in the credential system.
    And I reset the credential cache
    When I verify the password "KrabbyPatty" for user "spongebob"
    Then the result will be "false"
  
  Scenario: Delete a User
    Given user "spongebob" exists in the credential system.
    When I delete the user "spongebob"
    And I reset the credential cache
    Then the user "spongebob" should not be found in the credential system.

