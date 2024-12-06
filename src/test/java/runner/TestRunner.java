package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
  features = "src/test/resources/hellocucumber",
  glue = {"hellocucumber.steps"},
//        tags = "localRunOnly",
  plugin = {"pretty", "html:target/cucumber-report.html", "json:target/cucumber-report/cucumber.json", "junit:target/cucumber-report/cucumber.xml", "timeline:target/timeline", "rerun:target/failureScenarios.txt"}
)

public class TestRunner extends AbstractTestNGCucumberTests {
  @Override
  @DataProvider(parallel = true)
  public Object[][] scenarios() {
    return super.scenarios();
  }
}