package hellocucumber.pages;

import hellocucumber.locators.BasicPageLocators;
import hellocucumber.locators.LoginPageLocators;
import hellocucumber.utilities.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class LoginPage extends BasePage {
    public LoginPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(LoginPage.class);
    private final Map<String, String> xpathToElement = LoginPageLocators.createLibraryElement();

    public void verifyLoginPage() {
        waitForPageLoaded();
        waitForSpinnerCDP(1);
        //        cookiePopup();
        String selector = xpathToElement.get("username");
        waitForVisibilityOfElementLocated(By.xpath(selector), 15);
        LOG.info("User is on Login page");
    }

    public void enterTextLogin(String text, String textfield) {
        enterText(text, xpathToElement.get(textfield), textfield);
    }

    public void clickButton(String button) {
        waitForSpinnerCDP(1);
        String locator = xpathToElement.get(button);
        clickOrEvaluateAndClick(locator);
        try {
            waitForPageLoaded();
            waitForSpinnerCDP(1);
        } catch (StaleElementReferenceException e) {
            LOG.info("StaleElementReferenceException raised in cdpLoginPage.clickButton method.");
        }
    }

}
