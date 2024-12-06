package hellocucumber.steps;

import hellocucumber.pages.LoginPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.java.en.When;

public class LoginSteps {
    private final LoginPage loginPage;

    public LoginSteps() {
        loginPage = new LoginPage(DriverUtil.getDriver());
    }

    @When("the user logs in as {string} and {string} on the login page")
    public void the_user_logs_in_as_string_and_string_on_the_login_page(String username, String passwordOrPin) {
        loginPage.verifyLoginPage();
        loginPage.enterTextLogin(username, "username");
        loginPage.enterTextLogin(passwordOrPin, "password");
        loginPage.clickButton("submit");
    }
}
