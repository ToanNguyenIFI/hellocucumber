package hellocucumber.steps;

import hellocucumber.pages.BasicPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class StepDefinitions {
    private final BasicPage basicPage;

    public StepDefinitions() {
        basicPage = new BasicPage(DriverUtil.getDriver());
    }

    @When("all step definitions are implemented")
    public void allStepDefinitionsAreImplemented() {
    }

    @Then("the scenario passes")
    public void theScenarioPasses() {
    }

}
