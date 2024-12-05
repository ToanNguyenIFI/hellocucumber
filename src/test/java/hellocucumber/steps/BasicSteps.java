package hellocucumber.steps;

import hellocucumber.pages.BasicPage;
import hellocucumber.utilities.DriverUtil;
import hellocucumber.utilities.TestDataLoader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BasicSteps {
  private final BasicPage basicPage;

  private static final Logger LOG = LogManager.getLogger(BasicPage.class);

  public BasicSteps() {
    basicPage = new BasicPage(DriverUtil.getDriver());
  }

  @Given("the user can open {string}")
  public void the_user_can_open_string(String url) {
    basicPage.openURL(url);
  }

  @When("the user refresh current page")
  public void the_user_refresh_current_page() {
    basicPage.refresh();
  }

  @Given("the user switches to browser {string}")
  public void the_user_switches_to_browser_string(String browserName) {
    DriverUtil.switchToBrowser(TestDataLoader.getTestData(browserName));
  }

  @Then("the user closes browser {string}")
  public void the_user_close_browser(String browserName) {
    DriverUtil.closeBrowser(browserName);
  }

  @Then("current page url contains {string}")
  public void current_page_url_contains(String urlPart) {
    basicPage.currentPageUrlContains(urlPart);
  }

  @Then("the user sees text {string}")
  public void the_user_sees_text(String text) {
    basicPage.currentPageHasText(text);
  }
  @And("the user closes current page")
  public void the_user_closes_current_page() {
    basicPage.closeCurrentPage();
  }

  @And("the user saves {string} with value {string}")
  public void the_user_saves_string_with_value_string(String key, String value) {
    basicPage.saveKeyWithValue(key, value);
  }
}
