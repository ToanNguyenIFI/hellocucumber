package hellocucumber.pages;

import hellocucumber.locators.BasicPageLocators;
import hellocucumber.locators.CheckoutPageLocators;
import hellocucumber.locators.LoginPageLocators;
import hellocucumber.utilities.BasePage;
import hellocucumber.utilities.TestDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class CheckoutPage extends BasePage {
    public CheckoutPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(CheckoutPage.class);
    private final Map<String, String> xpathToPage = CheckoutPageLocators.createLibraryPage();
    private final Map<String, String> xpathToElement = CheckoutPageLocators.createLibraryElement();

    public void enterText(String text, String textfield) {
        enterText(text, xpathToElement.get(textfield), textfield);
    }

    public void waitAndClickButton(String buttonText) {
        clickOrEvaluateAndClick(xpathToElement.get(TestDataLoader.getTestData(buttonText)));
        waitForSpinnerCDP(1);
        LOG.info("Clicked on button \"{}\"", buttonText);
    }

    public void verifyCheckoutPage() {
        waitForVisibilityOfElementLocated(By.xpath(xpathToPage.get("page title")));
        LOG.info("User is on checkout page");
    }

}
