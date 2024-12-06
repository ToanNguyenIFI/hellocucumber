package hellocucumber.steps;

import hellocucumber.pages.ProductPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.java.en.And;

public class ProductSteps {
  private final ProductPage productPage;
  public static final String CONTEXT = "products page";

  public ProductSteps() {
    productPage = new ProductPage(DriverUtil.getDriver());
  }

  @And("the user is on " + CONTEXT)
  public void the_user_is_on_products_page() {
    productPage.verifyProductPage();
  }

  @And("the user selects {string} package on " + CONTEXT)
  public void the_user_selects_string_package_on_products_page(String packageName) {
    productPage.choosePackage(packageName);
  }

  @And("the user clicks button {string} on " + CONTEXT)
  public void the_user_clicks_button_string_on_products_page(String buttonText) {
    productPage.waitAndClickButton(buttonText);
  }

}
