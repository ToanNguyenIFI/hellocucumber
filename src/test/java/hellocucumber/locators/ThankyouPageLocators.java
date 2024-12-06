package hellocucumber.locators;

import java.util.HashMap;
import java.util.Map;

public class ThankyouPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("page title", "//span[@data-test='title']");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("thankyou", "//h2[@class='complete-header']");
        return xpathToElement;
    }
}
