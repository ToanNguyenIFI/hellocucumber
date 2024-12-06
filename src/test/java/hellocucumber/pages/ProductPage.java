package hellocucumber.pages;

import hellocucumber.locators.ProductPageLocators;
import hellocucumber.utilities.BasePage;
import hellocucumber.utilities.TestDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public class ProductPage extends BasePage {
    public ProductPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(ProductPage.class);
    private final Map<String, String> xpathToElement = ProductPageLocators.createLibraryElement();
    private final Map<String, String> xpathToPage = ProductPageLocators.createLibraryPage();

    public void verifyProductPage() {
        waitForVisibilityOfElementLocated(By.xpath(xpathToPage.get("page title")));
        LOG.info("User is on Products page");
    }

    public void choosePackage(String packageName) {
        packageName = TestDataLoader.getTestData(packageName);
        String selector;
        waitForPageLoaded();
        selector = String.format(xpathToElement.get("item"), packageName);
        LOG.info(selector);
        clickOrEvaluateAndClick(selector);
        waitForSpinnerCDP(1);
        LOG.info("package {} booked successfully", packageName);
    }

    public void waitAndClickButton(String buttonText) {
        clickOrEvaluateAndClick(xpathToElement.get(TestDataLoader.getTestData(buttonText)));
        waitForSpinnerCDP(1);
        LOG.info("Clicked on button \"{}\"", buttonText);
    }

}
