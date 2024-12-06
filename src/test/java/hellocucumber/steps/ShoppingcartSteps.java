package hellocucumber.steps;

import hellocucumber.pages.ShoppingcartPage;
import hellocucumber.utilities.DriverUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;

import java.util.List;

public class ShoppingcartSteps {
  private final ShoppingcartPage shoppingcartPage;
  public static final String CONTEXT = "shopping cart";

  public ShoppingcartSteps() {
    shoppingcartPage = new ShoppingcartPage(DriverUtil.getDriver());
  }

  @And("the user sees following texts on " + CONTEXT)
  public void the_user_sees_following_texts_on_shopping_cart(DataTable textList) {
    List<String> infoList = textList.asList(String.class);
    shoppingcartPage.verifyInformationDisplay("texts",infoList);
  }

  @And("the user clicks button {string} on " + CONTEXT)
  public void the_user_clicks_button_string_on_shopping_cart(String buttonText) {
    shoppingcartPage.waitAndClickButton(buttonText);
  }

}
