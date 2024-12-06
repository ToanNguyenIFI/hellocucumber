package hellocucumber.locators;

import java.util.HashMap;
import java.util.Map;

public class CheckoutPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("page title", "//span[@data-test='title']");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("First Name", "//input[@id='first-name']");
        xpathToElement.put("Last Name", "//input[@id='last-name']");
        xpathToElement.put("Postal Code", "//input[@id='postal-code']");
        xpathToElement.put("continue", "//input[@id='continue']");
        xpathToElement.put("finish", "//button[@id='finish']");
        return xpathToElement;
    }
}
