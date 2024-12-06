Feature: An example

  @TEST-123
  Scenario: The example
    Given the user can open "@TD:LoginPage"
    When the user logs in as "@TD:username" and "@TD:password" on the login page
    And the user is on products page
    And the user selects "@TD:Sauce_Labs_Backpack" package on products page
    And the user clicks button "shoppingcart" on products page
    And the user sees following texts on shopping cart
      | @TD:Sauce_Labs_Backpack|
    And the user clicks button "checkout" on shopping cart
    And the user is on checkout page
    And the user enters customer information on checkout page
     | First Name  | @TD:firstname  |
     | Last Name   | @TD:lastname   |
     | Postal Code | @TD:postalcode |
    And the user clicks button "continue" on checkout page
    And the user clicks button "finish" on checkout page
    And the user is on thankyou page

