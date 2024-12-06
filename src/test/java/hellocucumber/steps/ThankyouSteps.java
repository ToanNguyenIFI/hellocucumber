package hellocucumber.steps;

import hellocucumber.pages.ThankyouPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.java.en.And;

public class ThankyouSteps {
  private final ThankyouPage thankyouPage;
  public static final String CONTEXT = "thankyou page";

  public ThankyouSteps() {
    thankyouPage = new ThankyouPage(DriverUtil.getDriver());
  }

  @And("the user is on " + CONTEXT)
  public void the_user_is_on_products_page() {
    thankyouPage.verifyThankyouPage();
  }

}
