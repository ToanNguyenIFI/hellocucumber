Feature: Sales journey

  @SCRUM-3
  Scenario:Customer creation with one package
    # Step 1: Launch the url "https://www.saucedemo.com/" via web
    Given the user can open "@TD:LoginPage"

    # Step 2: Enters username and password, then clicks on login button
    When the user logs in as "@TD:username" and "@TD:password" on the login page
    And the user is on products page

    # Step 3: Select one package on the products page
    And the user selects "@TD:Sauce_Labs_Backpack" package on products page

    # Step 4: Click on icon "shopping cart" on the products page
    And the user clicks button "shoppingcart" on products page
    And the user sees following texts on shopping cart
      | @TD:Sauce_Labs_Backpack|

    # Step 5: Click on button "checkout" on shopping cart
    And the user clicks button "checkout" on shopping cart
    And the user is on checkout page

    # Step 6: Enter customer information on checkout page
    And the user enters customer information on checkout page
     | First Name  | @TD:firstname  |
     | Last Name   | @TD:lastname   |
     | Postal Code | @TD:postalcode |

    # Step 7: Click on button "continue"
    And the user clicks button "continue" on checkout page

    # Step 8: Click on button "finish"
    And the user clicks button "finish" on checkout page
    And the user is on thankyou page

