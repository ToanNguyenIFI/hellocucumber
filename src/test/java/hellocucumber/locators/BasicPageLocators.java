package hellocucumber.locators;

import java.util.HashMap;
import java.util.Map;

public class BasicPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("my sky contract", "//h1[@class='headlineTitle']/descendant::span[contains(text(),'Vertrag')]");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("New", "//li[@data-name='New']");
        return xpathToElement;
    }
}
