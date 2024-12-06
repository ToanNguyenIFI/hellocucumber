package hellocucumber.pages;

import hellocucumber.locators.ShoppingcartPageLocators;
import hellocucumber.utilities.BasePage;
import hellocucumber.utilities.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static hellocucumber.steps.Hook.testedEnv;

public class ShoppingcartPage extends BasePage {
    public ShoppingcartPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(ShoppingcartPage.class);
    private final Map<String, String> xpathToElement = ShoppingcartPageLocators.createLibraryElement();

    public void verifyProductPage() {
        waitForVisibilityOfElementLocated(By.xpath(xpathToElement.get("page title")));
        LOG.info("User is on shopping cart page");
    }
    public void verifyInformationDisplay(String informationType, List<String> informationList) {
        informationList = convertToTestDataWithNoSpecialChar(informationList);
        waitForPageLoaded();
        StringBuilder pageContent = new StringBuilder(getTextOfElement(By.xpath("//body")));
        LOG.info("Page content is {}", pageContent);
        for (String info : informationList) {
            try {
                Assert.assertTrue(
                        String.format("The page content does not contains: %s", info),
                        StringUtils.containsIgnoreCase(pageContent.toString(), info));
                LOG.info("This \"{}\" {} displays as expected", info, informationType);
            } catch (AssertionError e) {
                LOG.info(
                        "This \"{}\" {} is NOT displays as expected. Trying to scroll down and verify again",
                        info,
                        informationType);
                scrollToBottom(200);
                waitForPageLoaded();
                pageContent = new StringBuilder(getTextOfElement(By.xpath("//body")));
                Assert.assertTrue(
                        String.format("The page content does not contains: %s", info),
                        pageContent.toString().contains(info));
            }
        }
    }

    public void waitAndClickButton(String buttonText) {
        clickOrEvaluateAndClick(xpathToElement.get(TestDataLoader.getTestData(buttonText)));
        waitForSpinnerCDP(1);
        LOG.info("Clicked on button \"{}\"", buttonText);
    }

}
