package hellocucumber.locators;

import java.util.HashMap;
import java.util.Map;

public class LoginPageLocators {
    public static Map<String, String> createLibraryPage() {
        Map<String, String> xpathToPage = new HashMap<>();
        xpathToPage.put("login title", "//div[@class='login_logo']");

        return xpathToPage;
    }

    public static Map<String, String> createLibraryElement() {
        Map<String, String> xpathToElement = new HashMap<>();
        xpathToElement.put("username", "//input[@id='user-name']");
        xpathToElement.put("password", "//input[@id='password']");
        xpathToElement.put("submit", "//input[@id='login-button']");
        return xpathToElement;
    }
}
