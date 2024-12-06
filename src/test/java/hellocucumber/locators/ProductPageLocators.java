package hellocucumber.locators;

import java.util.HashMap;
import java.util.Map;

public class ProductPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("page title", "//span[@data-test='title']");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("item", "//div[text()='%s']//ancestor::div[@class='inventory_item_description']//button[contains(@id, 'add-to-cart')]");
        xpathToElement.put("shoppingcart", "//a[@class='shopping_cart_link']");
        return xpathToElement;
    }
}
