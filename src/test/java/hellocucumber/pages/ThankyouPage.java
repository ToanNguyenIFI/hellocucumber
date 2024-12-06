package hellocucumber.pages;

import hellocucumber.locators.ThankyouPageLocators;
import hellocucumber.utilities.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class ThankyouPage extends BasePage {
    public ThankyouPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(ThankyouPage.class);
    private final Map<String, String> xpathToElement = ThankyouPageLocators.createLibraryElement();

    public void verifyThankyouPage() {
        waitForVisibilityOfElementLocated(By.xpath(xpathToElement.get("thankyou")));
        LOG.info("User is on thank you page");
    }

}
