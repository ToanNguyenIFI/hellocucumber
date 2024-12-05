package hellocucumber.steps;

import hellocucumber.pages.BasicPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StepDefinitions {
    private final BasicPage basicPage;

    private static final Logger LOG = LogManager.getLogger(BasicPage.class);

    public StepDefinitions() {
        basicPage = new BasicPage(DriverUtil.getDriver());
    }

    @Given("the user can open {string}")
    public void the_user_can_open_string(String url) {
        basicPage.openURL(url);
    }

    @When("all step definitions are implemented")
    public void allStepDefinitionsAreImplemented() {
    }

    @Then("the scenario passes")
    public void theScenarioPasses() {
    }

}
