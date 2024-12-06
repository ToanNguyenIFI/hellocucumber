package hellocucumber.steps;

import hellocucumber.pages.CheckoutPage;
import hellocucumber.pages.LoginPage;
import hellocucumber.utilities.DriverUtil;
import hellocucumber.utilities.TestDataLoader;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

public class CheckoutSteps {
    private final CheckoutPage checkoutPage;
    public static final String CONTEXT = "checkout page";

    public CheckoutSteps() {
        checkoutPage = new CheckoutPage(DriverUtil.getDriver());
    }

    @And("the user enters customer information on " + CONTEXT)
    public void the_user_enters_customer_information_on_checkout_page(DataTable dateTable) {
        Map<String, String> cusData = dateTable.asMap(String.class, String.class);
        for (String cusFieldName : cusData.keySet()) {
            checkoutPage.enterText(TestDataLoader.getTestData(cusData.get(cusFieldName)), cusFieldName);
        }
    }

    @And("the user is on " + CONTEXT)
    public void the_user_is_on_checkout_page() {
        checkoutPage.verifyCheckoutPage();
    }

    @And("the user clicks button {string} on " + CONTEXT)
    public void the_user_clicks_button_string_on_checkout_page(String buttonText) {
        checkoutPage.waitAndClickButton(buttonText);
    }
}
