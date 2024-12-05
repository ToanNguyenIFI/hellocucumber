package hellocucumber.utilities;

/*
    All basic functions are stored in here like enter text, click element, select from Dropdown etc.
    If you have additional basic functions please enter in here as all Page classes are extended from BasePage
*/

import hellocucumber.modal.Directions;
import io.cucumber.core.exception.CucumberException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hellocucumber.steps.Hook.testedEnv;

public class BasePage {

    public static final String REGEX_TO_FIND_LAST_DOT = "[.](?=[^.]*$)";
    public static final ThreadLocal<String> CURRENT_THREAD_SCENARIO = new ThreadLocal<>();
    private static final Logger LOG = LogManager.getLogger(BasePage.class);
    private static final int WAIT_TIMEOUT = 60;
    public static final ThreadLocal<RemoteWebDriver> threadLocalDriverBasePage = new ThreadLocal<>();
    private static final Random r = new Random();

    public BasePage(RemoteWebDriver driver) {
        threadLocalDriverBasePage.set(DriverUtil.threadLocalActiveBrowsers.get().getOrDefault("current", driver));
    }

    public void enterText(String text, String locatorTextField, String textField) {
        text = TestDataLoader.getTestData(text);
        WebElement textFieldElem = verifyVisibilityOfElement(textField, locatorTextField);

        if (!textFieldElem.isDisplayed()) {
            throw new CucumberException("TextField " + textField + " not found!");
        }
        if ((textFieldElem.getText() != null && !textFieldElem.getText().isEmpty())
                || (textFieldElem.getAttribute("value") != null
                && !textFieldElem.getAttribute("value").isEmpty())) {
            clearTextOnTextField(textFieldElem);
        }
        textFieldElem.sendKeys(text);
        if (textField.contains("assword")
                || textField.contains("PW")
                || textField.contains("pw")) {
            LOG.info("\"********\" entered into TextField \"{}\" ", textField);
        } else {
            LOG.info("\"{}\" entered into TextField \"{}\" ", text, textField);
        }
    }

    private void clearTextOnTextField(WebElement textFieldElem) {
        textFieldElem.sendKeys(Keys.CONTROL + "a");
        textFieldElem.sendKeys(Keys.DELETE);
        int attempts = 0;
        do {
            if ((textFieldElem.getText() == null || textFieldElem.getText().isEmpty())
                    && (textFieldElem.getAttribute("value") == null
                    || textFieldElem.getAttribute("value").isEmpty())) break;
            waitFor(250).milliseconds();
            LOG.info("Trying to clear text field again");
            textFieldElem.clear();
            attempts++;
        } while (attempts < 10);
        if (attempts == 10) LOG.info("Cannot clear text field after 10 times trying");
    }

    public void clickWebElement(String locatorWebElement, String webElement) {
        waitForPageLoaded();
        if (locatorWebElement == null || locatorWebElement.isEmpty()) {
            throw new CucumberException("no Locator given for " + webElement);
        }
        verifyVisibilityOfElement(webElement, locatorWebElement).click();
        LOG.info("clicked Button {}", webElement);
    }

    public void clickWebElementJS(String locatorWebElement, String webElement) {
        if (locatorWebElement == null || locatorWebElement.isEmpty()) {
            throw new CucumberException("no Locator given for " + webElement);
        }
        WebElement welToClickOn = waitForPresenceOfElementLocated(By.xpath(locatorWebElement));
        (threadLocalDriverBasePage.get()).executeScript("arguments[0].click();", welToClickOn);
        try {
            waitForPageLoaded();
        } catch (WebDriverException e) {
            waitForPageLoaded();
        }

        LOG.info("clicked Button {}", webElement);
    }

    public WebElement waitForElementToBeClickable(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    public WebElement waitForElementToBeClickable(By by) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(WAIT_TIMEOUT));
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    public WebElement waitForVisibilityOfElementLocated(By by) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(WAIT_TIMEOUT));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public Boolean waitForInvisibilityOfElementLocated(By by) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(WAIT_TIMEOUT));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public Boolean waitForInvisibilityOfElementLocated(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public WebElement waitForPresenceOfElementLocated(By by) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(WAIT_TIMEOUT));
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public List<WebElement> waitForPresenceOfElementsLocated(By by) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(60));
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    public Boolean waitUntilElementHasAttributeContains(
            WebElement element, String attribute, String value, int waitTimeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(waitTimeout));
        return wait.until(ExpectedConditions.attributeContains(element, attribute, value));
    }

    public Boolean waitUntilElementAttributeNotContains(
            WebElement element, String attribute, String value, int waitTimeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(waitTimeout));
        return wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(element, attribute, value)));
    }

    public Boolean waitUntilElementHaveAttributeValueEquals(
            By locator, String attribute, String value, int waitTimeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(waitTimeout));
        return wait.until(ExpectedConditions.attributeToBe(locator, attribute, value));
    }

    public Boolean waitUntilElementContainsTexts(By by, String value, int waitTimeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(waitTimeout));
        return wait.until(
                ExpectedConditions.textToBePresentInElement(waitForVisibilityOfElementLocated(by, waitTimeout), value));
    }

    public void waitForPageLoaded() {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(50));
        wait.until(wd -> ((threadLocalDriverBasePage.get())
                .executeScript("return document.readyState")
                .equals("complete")));
        int count = 0;
        if ((boolean) (threadLocalDriverBasePage.get()).executeScript("return window.jQuery != undefined")) {
            while (!(boolean) (threadLocalDriverBasePage.get()).executeScript("return jQuery.active == 0")) {
                waitFor(1).seconds();
                if (count > 400) break;
                count++;
            }
        }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@class='loadingIndicatorIcon']")));
    }

    public void selectFromDropdownByVisibleTextEQUAL(String locatorWebElement, String valueToSelect) {
        if (locatorWebElement == null || locatorWebElement.isEmpty()) {
            throw new CucumberException("no Locator given for Dropdown");
        }
        Select select = new Select(waitForPresenceOfElementLocated(By.xpath(locatorWebElement)));
        select.selectByVisibleText(TestDataLoader.getTestData(valueToSelect));
    }

    public void selectFromDropdownByIndex(String locatorWebElement, String index) {
        Select select = new Select(waitForPresenceOfElementLocated(By.xpath(locatorWebElement)));
        select.selectByIndex(Integer.parseInt(index));
    }

    public void selectFromDropdownByValueEQUAL(String locatorWebElement, String valueToSelect) {
        if (locatorWebElement == null || locatorWebElement.isEmpty()) {
            throw new CucumberException("no Locator given for Dropdown");
        }
        Select select = new Select(waitForVisibilityOfElementLocated(By.xpath(locatorWebElement)));
        select.selectByValue(valueToSelect);
    }

    public void selectFromListByVisibleTextEQUAL(String dropdownLocator, String valueToSelect) {
        if (dropdownLocator == null || dropdownLocator.isEmpty()) {
            throw new CucumberException("no Locator given for Dropdown");
        }
        clickOrEvaluateAndClick(dropdownLocator);
        assertAndVerifyElement(By.xpath(dropdownLocator));
        waitForSpinnerSF(1);
        waitForSpinnerCDP(1);
        WebElement element = null;
        List<WebElement> multipleElements = threadLocalDriverBasePage
                .get()
                .findElements(By.xpath(
                        "//*[@role='presentation' or @role='menuitem' or @role='option' or @role='combobox']//*[text()='"
                                + valueToSelect + "' and not(ancestor::a[@*='tab-name'])][1] | //option[text()='"
                                + valueToSelect + "']"));
        for (WebElement e : multipleElements) {
            boolean displayed = e.isDisplayed();
            boolean enabled = e.isEnabled();
            LOG.info("is displayed: {}", displayed);
            LOG.info("is enabled: {}", enabled);
            if (enabled && displayed) {
                element = e;
                break;
            }
        }
        if (element != null) {
            scrollTo(element);
            element.click();
        } else throw new CucumberException("Cannot find option. Please recheck!");
    }

    public void waitForNewTabSky(int expectedTabs, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        wait.until(ExpectedConditions.urlContains("sky"));
        int counter = 0;
        while (true) {
            try {
                Set<String> winId = threadLocalDriverBasePage.get().getWindowHandles();
                if (winId.size() >= expectedTabs) {
                    return;
                }
                counter++;
                if (counter > timeout) {
                    return;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return;
            }
        }
    }

    public void clickButtonByText(String buttonName) {
        String buttonXpath = String.format("//*[text()='%s']", buttonName);
        clickOrEvaluateAndClick(buttonXpath);
        LOG.info("Click on a button with text \"{}\"", buttonName);
    }

    public void executeJs(String js) {
        threadLocalDriverBasePage.get().executeScript(js);
    }

    public void executeJs(String js, WebElement e) {
        threadLocalDriverBasePage.get().executeScript(js, e);
    }

    public void scrollTo(WebElement webElement) {
        try {
            executeJs("arguments[0].scrollIntoView(false);", webElement);
        } catch (Exception e) {
            waitFor(5).seconds();
            executeJs("arguments[0].scrollIntoView(false);", webElement);
        }
    }

    public void scrollToViaAction(WebElement webElement) {
        new Actions(threadLocalDriverBasePage.get()).moveToElement(webElement).perform();
    }

    public ScrollBuilder scrollInsideElement(String elementXpath) {
        return new ScrollBuilder(elementXpath);
    }


    public class ScrollBuilder {
        private final String elementXpath;
        private Directions direction;
        private String script;
        private int loopCount = 1;

        public ScrollBuilder(String elementXpath) {
            this.elementXpath = elementXpath;
        }

        public ScrollBuilder to(Directions direction) {
            this.direction = direction;
            switch (direction) {
                case TOP:
                    this.script = "arguments[0].scrollTo(0, 0);";
                    break;
                case BOTTOM:
                    this.script = "arguments[0].scrollTo(0, arguments[0].scrollHeight);";
                    break;
                default:
                    LOG.info("Directions supported available are: {}, {}", Directions.TOP, Directions.BOTTOM);
                    LOG.info("Other directions will be developed in need. Thanks for using!");
                    throw new CucumberException(
                            String.format("Direction is not supported in this version: [%s]", direction));
            }
            return this;
        }

        public ScrollBuilder withLoop(int loopCount) {
            this.loopCount = loopCount;
            return this;
        }

        public void perform() {
            Assert.assertTrue(
                    String.format(
                            "Wrong number of loop: [%s]. Please pass the integer value greater than 0!",
                            this.loopCount),
                    this.loopCount > 0);
            for (int loop = 0; loop < this.loopCount; loop++) {
                executeJs(script, waitForVisibilityOfElementLocated(By.xpath(this.elementXpath)));
                LOG.info("User scrolls to {} the {} time(s)", this.direction, loop + 1);
                waitFor(1).seconds();
            }
        }
    }

    public void scrollToTop() {
        executeJs("window.scrollTo(0, 0);");
        waitFor(500).milliseconds();
    }

    public void scrollElementToCenter(WebElement e) {
        String scrollElementIntoMiddle =
                "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                        + "var elementTop = arguments[0].getBoundingClientRect().top;"
                        + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

        threadLocalDriverBasePage.get().executeScript(scrollElementIntoMiddle, e);
        waitFor(500).milliseconds();
    }

    public void waitForSpinnerPaypal() {
        try {
            LOG.info("checking for Loading Spinner");
            WebDriverWait wait =
                    new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(10), Duration.ofMillis(1000));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'spinnerWithLockIcon')]")));
        } catch (Exception ignored) {
            LOG.info("Skipping wait for Loading Spinner");
        }
    }

    public void waitForSpinnerCDP(int waitTimeForSpinner) {
        String spinnerXpath = "//*[contains(@data-testid,'spinner')]|//*[contains(@class,'is-loading')]";
        try {
            waitForVisibilityOfElementLocated(By.xpath(spinnerXpath), waitTimeForSpinner);
            LOG.info("Loading Spinner is visible. Wait until Spinner disappears...");
            waitForInvisibilityOfElementLocated(By.xpath(spinnerXpath), 60);
            LOG.info("Loading Spinner is invisible");
        } catch (Exception ignored) {
            LOG.info("Loading Spinner has not appeared");
        }
    }

    public void waitForSpinnerSF(int waitTimeForSpinner) {
        String spinnerXpath =
                "(//div[contains(@class,'loadingSpinner slds-spinner_container') or contains(@class,'slds-spinner_large') or contains(@class,' slds-spinner_medium') or contains(@class,'  forceInlineSpinner')])[last()]";
        try {
            waitForVisibilityOfElementLocated(By.xpath(spinnerXpath), waitTimeForSpinner);
            LOG.info("Loading Spinner is visible. Wait until Spinner disappears...");
            waitForInvisibilityOfElementLocated(By.xpath(spinnerXpath), 300);
            LOG.info("Loading Spinner is invisible");
        } catch (Exception ignored) {
            LOG.info("Loading Spinner has not appeared");
        }
    }

    public void switchFrame(String idOrName) {
        threadLocalDriverBasePage.get().switchTo().frame(idOrName);
    }

    public void currentPageHasText(String text) {
        waitForPageLoaded();
        List<String> textList = new ArrayList<>();
        getStringNoSpecialChar(textList, text);
        String currentExecutingEnv = testedEnv.toLowerCase();
        for (String eachText : textList) {
            String selector = String.format("(//*[text()='%s' or contains(text(), '%s')])[1]", eachText, eachText);
            if (currentExecutingEnv.equalsIgnoreCase("dev") && eachText.contains("Sky Stream Box (Internet)")) {
                // Special condition for "dev" environment
                LOG.info("Cdp modal box has special content for dev environment: Sky Stream (Internet)");
                selector = "(//*[text()='Sky Stream (Internet)' or contains(text(), 'Sky Stream (Internet)')])[1]";
            }
            try {
                waitForVisibilityOfElementLocated(By.xpath(selector), 120);
            } catch (Exception e) {
                scrollToBottom(100);
                waitForVisibilityOfElementLocated(By.xpath(selector), 240);
            }
            LOG.info("Current page contains an element with text \"{}\".", eachText);
        }
    }

    public void currentPageHasTextDependingOnEnv(String requiredEnv, String text) {
        String currentExecutingEnv = testedEnv.toLowerCase();
        if (requiredEnv.toLowerCase().lastIndexOf(currentExecutingEnv) == -1)
            LOG.info(
                    "Current executing environment \"{}\" does not require to check texts in ({}) hence skipping it...",
                    currentExecutingEnv,
                    requiredEnv);
        else {
            LOG.info("Current executing environment \"{}\" starts to check...", currentExecutingEnv);
            waitForPageLoaded();
            if (text.toUpperCase().startsWith("@TD:")) {
                text = TestDataLoader.getTestData(text);
            }
            String selector = String.format("//main//*[text()='%s' or contains(text(), '%s')]", text, text);
            try {
                waitForVisibilityOfElementLocated(By.xpath(selector), 120);
            } catch (Exception e) {
                scrollToBottom(100);
                waitForVisibilityOfElementLocated(By.xpath(selector), 240);
            }
            LOG.info("Current page contains an element with text \"{}\".", text);
        }
    }

    public void currentPageHasNoText(String text) {
        text = TestDataLoader.getTestData(text);
        String selector =
                String.format("//*[text()='%s' or contains(text(), '%s')]|//p[contains(.,'%s')]", text, text, text);
        try {
            scrollToBottom(200);
            waitForVisibilityOfElementLocated(By.xpath(selector), 5);
            scrollToTop();
            waitForVisibilityOfElementLocated(By.xpath(selector), 5);
            throw new CucumberException(String.format(
                    "Current page contains an element with text \"%s\". Not present expected.", selector));
        } catch (NoSuchElementException | TimeoutException e) {
            LOG.info("Current page does not contain an element with text \"{}\" as expected.", selector);
        }
    }

    public void verifyPageHasContent(String text) {
        if (text.startsWith("@TD:")) {
            text = TestDataLoader.getTestData(text);
        }
        scrollToBottom(200);
        String pageContent = getTextOfElement(By.xpath("//body"));
        LOG.info("Page content is {}", pageContent);
        Assert.assertTrue(
                String.format("Current page does not contains text %s as expected", text), pageContent.contains(text));
        LOG.info("Current page contains text \"{}\".", text);
    }

    public void elementHasAttribute(String selector, String attribute) {
        WebElement element = waitForVisibilityOfElementLocated(By.xpath(selector));
        Assert.assertNotNull(
                String.format(
                        "Element with locator \"%s\" does not have attribute \"%s\". Please recheck!",
                        selector, attribute),
                element.getAttribute(attribute));
    }

    public boolean elementsIsDisplayed(String selector) {
        try {
            List<WebElement> listElements = waitForVisibilityOfAllElementsLocated(By.xpath(selector), 15);
            if (!listElements.isEmpty()) {
                LOG.info("Elements \"{}\" are displayed", selector);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void elementHasNotAttribute(String selector, String attribute) {
        WebElement element = waitForVisibilityOfElementLocated(By.xpath(selector));
        Assert.assertNull(
                String.format("Element \"%s\" still has \"%s\" attribute.", selector, attribute),
                element.getAttribute(attribute));
    }

    public void verifyButtonStatus(String buttonText, String status) {
        String locator = String.format("//button[@aria-label='%s']", buttonText);
        if (status.equals("enabled")) elementHasNotAttribute(locator, "disabled");
        else if (status.equals("disabled")) elementHasAttribute(locator, "disabled");
        else throw new IllegalArgumentException("Status should be \"enabled\" or \"disabled\"");
        LOG.info("Button with text \"{}\" is {} as expected.", buttonText, status);
    }

    public void evaluateXpathAndClick(String xpath) {
        WebElement scrollElement = verifyVisibilityOfElement(xpath, xpath, 30);
        scrollElementToCenter(scrollElement);
        executeJs("arguments[0].click();", scrollElement);
    }

    public void clickOrEvaluateAndClick(String selector) {
        LOG.info("Element locator is {}", selector);
        try {
            waitForSpinnerCDP(2);
            clickWebElement(selector, selector);
        } catch (Exception | AssertionError e) {
            // To avoid changing of selector this solution may help...
            LOG.info("Found exception: {}", e.getMessage());
            evaluateXpathAndClick(selector);
            LOG.info("Clicked on evaluated xpath: \"{}\".", selector);
        }
    }

    public void selectOptionByVisibleText(String option, String dropdownLocator) {
        Select select = new Select(waitForVisibilityOfElementLocated(By.xpath(dropdownLocator)));
        select.selectByVisibleText(option);
    }

    public void verifyCurrentUrl(String expectedUrl) {
        waitForPageLoaded();
        expectedUrl = TestDataLoader.getTestData(expectedUrl);
        String actual = threadLocalDriverBasePage.get().getCurrentUrl();
        Assert.assertEquals(
                String.format("Current Url: [%s] is not expected [%s]", actual, expectedUrl), expectedUrl, actual);
    }

    public void clickBlankSpace() {
        clickOrEvaluateAndClick("//body");
        LOG.info("Clicking on a blank space on the page (body element).");
    }

    public void assertAndVerifyElement(By element) {
        boolean isPresent = false;
        for (int i = 0; i < 1000; i++) {
            try {
                if (threadLocalDriverBasePage.get().findElement(element) != null) {
                    isPresent = true;
                    break;
                }
            } catch (Exception ignored) {
                LOG.info("Can not find element: {}", element.toString());
            }
            waitFor(250).milliseconds();
        }
        Assert.assertTrue(isPresent);
    }

    public void actionOneClick(WebElement webElement) {
        Actions builder = new Actions(threadLocalDriverBasePage.get());
        builder.moveToElement(webElement).click(webElement).build().perform();
    }

    public String generateRandomWord(int wordLength) {
        StringBuilder sb = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) {
            char tmp = (char) ('a' + r.nextInt('z' - 'a'));
            sb.append(tmp);
        }
        return sb.toString().replace("go", "og").replace("ci", "ic").replace("sky", "yks");
    }

    public String getTextOfElement(By element) {
        return waitForVisibilityOfElementLocated(element).getText();
    }

    public WaitBuilder waitFor(int duration) {
        return new WaitBuilder(duration);
    }

    public class WaitBuilder {
        private final int duration;

        public WaitBuilder(int duration) {
            this.duration = duration;
        }

        public void seconds() {
            try {
                TimeUnit.SECONDS.sleep(duration);
                LOG.info("Wait for {} seconds", duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        public void minutes() {
            try {
                TimeUnit.MINUTES.sleep(duration);
                LOG.info("Wait for {} minutes", duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        public void milliseconds() {
            try {
                TimeUnit.MILLISECONDS.sleep(duration);
                LOG.info("Wait for {} milliseconds", duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        public void days() {
            try {
                TimeUnit.DAYS.sleep(duration);
                LOG.info("Wait for {} day(s)", duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void scrollToBottom() {
        executeJs("window.scrollTo(0,document.body.scrollHeight || document.documentElement.scrollHeight)");
        waitFor(500).milliseconds();
    }

    // Covert price display in UI to same format with response from api
    public String getPricesInCdp(String stringWithPrice) {
        Pattern pattern;
        if (stringWithPrice.contains("mtl")) {
            pattern = Pattern.compile("€ \\d* mtl.");
        } else {
            pattern = Pattern.compile("€ \\d*");
        }
        Matcher matcher = pattern.matcher(stringWithPrice);
        String price = "";
        while (matcher.find()) {
            price = matcher.group(0);
        }
        if (price.equals("")) price = stringWithPrice;
        return price;
    }

    public String generateRandomNumber(int length) {
        String value = RandomStringUtils.randomNumeric(length);
        TestDataLoader.setTestData("randomNumber", value);
        return value;
    }

    public String getCurrentTimeInGermany() {
        Clock clock = Clock.system(ZoneId.of("Europe/Berlin"));
        ZonedDateTime now = ZonedDateTime.now(clock);
        String currentTimeInGermany = now.toString().substring(11, 20);
        LOG.info("Current time in Germany: {}", currentTimeInGermany);
        return StringUtils.chop(currentTimeInGermany);
    }

    public List<String> getStringListOfElementVia(By locator, String valueType, String attributeOrCssValue) {
        waitForSpinnerSF(1);
        waitForPageLoaded();
        try {
            waitForVisibilityOfElementLocated(locator, 10);
        } catch (Exception e) {
            LOG.info("No element located by {}. The list will be empty.", locator);
        }
        List<WebElement> webElementList = threadLocalDriverBasePage.get().findElements(locator);
        List<String> result = new ArrayList<>();
        for (WebElement element : webElementList) {
            String value = null;
            if (valueType.equalsIgnoreCase("text")) {
                value = element.getText();
            } else if (valueType.equalsIgnoreCase("attribute")) {
                value = element.getAttribute(attributeOrCssValue);
            } else if (valueType.equalsIgnoreCase("cssValue")) {
                value = element.getCssValue(attributeOrCssValue);
            }
            if (value != null) {
                result.add(value);
            } else {
                LOG.info("Can not get value of this element");
            }
        }
        LOG.info("List of value is: {}", result);
        return result;
    }

    public WebElement waitForVisibilityOfElementLocated(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public List<WebElement> waitForVisibilityOfAllElementsLocated(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    public void scrollToBottom(int offset) {
        Long documentHeightBeforeScroll = getDocumentScrollHeight();
        Long documentHeightAfterScroll;
        int scrollHeight = 0;
        do {
            int scrollTimes = (int) (documentHeightBeforeScroll / offset);
            for (int i = 1; i <= scrollTimes; i++) {
                if (i == scrollTimes) {
                    documentHeightBeforeScroll = getDocumentScrollHeight();
                }
                executeJs(String.format("window.scrollTo(0, %s);", (i * offset) + scrollHeight));
                waitFor(1).seconds();
                LOG.info("Scroll down by {}", (i * offset) + scrollHeight);
            }
            scrollHeight += scrollTimes * offset;
            documentHeightAfterScroll = getDocumentScrollHeight();
        } while (!documentHeightBeforeScroll.equals(documentHeightAfterScroll));
    }

    public Long getDocumentScrollHeight() {
        return (Long) threadLocalDriverBasePage.get().executeScript("return document.documentElement.scrollHeight");
    }

    public void refresh() {
        threadLocalDriverBasePage.get().navigate().refresh();
        try {
            // wait for the alert to exist, then handle it and continue with refresh
            WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(5));
            wait.until(ExpectedConditions.alertIsPresent());
            threadLocalDriverBasePage.get().switchTo().alert().accept();
            threadLocalDriverBasePage.get().switchTo().parentFrame();
            waitForPageLoaded();
            waitForSpinnerSF(1);
        } catch (Exception ignored) {
            LOG.info("Can not handle the alert");
        }
    }

    public void clickBlankSpaceSf() {
        clickOrEvaluateAndClick("//section");
        LOG.info("Clicking on a blank space on the page (body element).");
    }

    public void clearAllTextOnField(String locator) {
        WebElement element = waitForVisibilityOfElementLocated(By.xpath(locator));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        LOG.info("Cleared all text from selected field");
    }

    public void clearWebField(WebElement element) {
        while (element.getAttribute("value") != null
                && !element.getAttribute("value").equals("")) {
            element.sendKeys(Keys.CONTROL, Keys.chord("a"));
            element.sendKeys(Keys.BACK_SPACE);
        }
    }

    // Press Enter key at element
    public void pressEnterAtLocation(String locator) {
        threadLocalDriverBasePage.get().findElement(By.xpath(locator)).sendKeys(Keys.ENTER);
    }

    // Verify visibility Of element
    public WebElement verifyVisibilityOfElement(String element, String elementXpath) {
        waitForPageLoaded();
        WebElement webElement;
        try {
            webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
            LOG.info("Current page has element {}", element, elementXpath);
        } catch (TimeoutException e) {
            LOG.info("Element {} not visible. Scroll to bottom and verify again", element);
            scrollToBottom(200);
            webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
            LOG.info("Current page has element {}", element);
        } catch (Exception e) {
            throw new CucumberException("Can not find the element after " + 60 + " seconds");
        }
        scrollTo(webElement);
        return webElement;
    }

    // Verify visibility Of element with dynamic timeout
    public WebElement verifyVisibilityOfElement(String element, String elementXpath, int timeout) {
        WebElement webElement;
        try {
            webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), timeout);
            LOG.info("Current page has element {}", element);
        } catch (TimeoutException e) {
            LOG.info("Element {} not visible. Scroll to bottom and verify again", element);
            scrollToBottom(200);
            webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
            LOG.info("Current page has element {}", element);
        } catch (Exception e) {
            throw new CucumberException("Can not find the element after " + (timeout + 30) + "seconds");
        }
        scrollTo(webElement);
        return webElement;
    }

    // Verify invisibility Of element
    public void verifyInvisibilityOfElement(String element, String elementXpath, int timeout) {
        try {
            scrollToTop();
            waitForInvisibilityOfElementLocated(By.xpath(elementXpath), timeout);
            scrollToBottom(200);
            waitForInvisibilityOfElementLocated(By.xpath(elementXpath), timeout);
            LOG.info("Current page does not have element {} as expected", element);
        } catch (TimeoutException e) {
            throw new CucumberException("Fail due to current page has element " + element);
        }
    }

    public void zoomInOut(String percent) {
        executeJs(String.format("document.body.style.zoom = '%s'", percent));
        LOG.info("Zoomed {} ", percent);
    }

    public void setIndexOfColumnsAreDisplayedInTable() {
        try {
            waitForVisibilityOfElementLocated(By.xpath(TestDataLoader.getTestData("@TD:listColumnLocator")), 5);
        } catch (TimeoutException e) {
            if (Boolean.parseBoolean(TestDataLoader.getTestData("@TD:isTableLocatedInsideElement"))) {
                scrollInsideElement("//flexipage-record-home-scrollable-column[contains(@id,'middleColumn')]")
                        .to(Directions.BOTTOM)
                        .withLoop(2)
                        .perform();
            } else scrollToBottom(200);
        }
        int index = 0;
        List<WebElement> allTableColumns =
                waitForPresenceOfElementsLocated(By.xpath(TestDataLoader.getTestData("@TD:listColumnLocator")));
        for (WebElement element : allTableColumns) {
            index++;
            LOG.info("Index of column {} is {}", element.getAttribute("innerText"), index);
            TestDataLoader.setTestData(element.getAttribute("innerText") + "_columnIndex", String.valueOf(index));
        }
    }

    public int getIndexOfRowInTableHasValue(String rowValue, String uniqueColumn) {
        rowValue = TestDataLoader.getTestData(rowValue);
        int index = 0;
        List<WebElement> allTableRows =
                waitForPresenceOfElementsLocated(By.xpath(TestDataLoader.getTestData("@TD:listRowLocator")));
        TestDataLoader.setTestData("isRowValuePresent", "false");
        for (WebElement element : allTableRows) {
            index++;
            if ((uniqueColumn.contains("Date") && element.getText().startsWith(rowValue))
                    || (!uniqueColumn.contains("Date")
                    && element.getAttribute("innerText").equals(rowValue))) {
                LOG.info("Index of row having value '{}' is '{}'", rowValue, index);
                TestDataLoader.setTestData("@TD:rowIndex", String.valueOf(index));
                TestDataLoader.setTestData("isRowValuePresent", "true");
                break;
            }
        }
        return index;
    }

    public String getCellValue(int columnIndex, int rowIndex, String expectedResult) {
        String result;
        String baseSelector = String.format(TestDataLoader.getTestData("@TD:cellValueLocator"), rowIndex, columnIndex);
        if (expectedResult.equalsIgnoreCase("checked") || expectedResult.equalsIgnoreCase("unchecked")) {
            if (isCheckboxChecked(
                    String.format(TestDataLoader.getTestData("@TD:cellValueLocator"), rowIndex, columnIndex),
                    TestDataLoader.getTestData("@TD:checkboxTagAttribute"),
                    TestDataLoader.getTestData("@TD:checkboxAttributeActiveValue"),
                    TestDataLoader.getTestData("@TD:checkboxAttributeInactiveValue"))) {
                LOG.info("Cell value is: checked");
                return "checked";
            } else {
                LOG.info("Cell value is: unchecked");
                return "unchecked";
            }
        }
        if (expectedResult.equalsIgnoreCase("empty"))
            result = waitForPresenceOfElementLocated(By.xpath(String.format(baseSelector, rowIndex, columnIndex)))
                    .getAttribute("innerText");
        else {
            result = threadLocalDriverBasePage
                    .get()
                    .findElement(By.xpath(String.format(baseSelector, rowIndex, columnIndex)))
                    .getAttribute("innerText");
        }
        LOG.info("Cell value is: \"{}\"", result);
        if (expectedResult.equalsIgnoreCase("empty") || expectedResult.equalsIgnoreCase("not empty")) {
            String cellValue = result.replaceAll("(?m)^\\s+$", "");
            if (cellValue.isEmpty()) return "empty";
            else return "not empty";
        }
        return result;
    }

    public boolean isCheckboxChecked(String locator, String tagAttribute, String activeValue, String inactiveValue) {
        WebElement element = verifyVisibilityOfElement(locator, locator);
        if (tagAttribute.isBlank() && activeValue.isBlank() && inactiveValue.isBlank()) {
            return element.isSelected();
        } else {
            Assert.assertNotNull("Class Attribute can not be null", tagAttribute);
            String actualAttributeValue = element.getAttribute(tagAttribute);
            if (activeValue != null && inactiveValue == null) {
                return (actualAttributeValue.equalsIgnoreCase(activeValue));
            } else if (activeValue == null && inactiveValue != null) {
                return (!actualAttributeValue.equalsIgnoreCase(inactiveValue));
            } else if (activeValue != null) {
                return actualAttributeValue.contains(activeValue) && !actualAttributeValue.contains(inactiveValue);
            } else {
                throw new CucumberException("Active value and inactive value can not be null at the same time");
            }
        }
    }

    public static class ConvertTimeZoneBuilder {
        private final String dateTimeInString;
        private String expectedTimeZone;
        private String expectedFormat;

        public ConvertTimeZoneBuilder(String dateTimeInString) {
            this.dateTimeInString = dateTimeInString;
        }

        public ConvertTimeZoneBuilder toTimeZone(String expectedTimeZone) {
            this.expectedTimeZone = expectedTimeZone == null ? "Europe/Berlin" : expectedTimeZone;
            return this;
        }

        public ConvertTimeZoneBuilder withFormat(String expectedFormat) {
            this.expectedFormat = expectedFormat;
            return this;
        }

        public String asString() {
            return LocalDateTime.ofInstant(Instant.parse(dateTimeInString), ZoneId.of(expectedTimeZone))
                    .format(DateTimeFormatter.ofPattern(expectedFormat));
        }

        public String toInstant() {
            String gmtDateTime = "";
            // Convert status as date time to instant format
            if (dateTimeInString.contains("T") && !dateTimeInString.contains("Z")) {
                LOG.info("Date time format \"{}\" is not correct. Convert to instant...", dateTimeInString);
                gmtDateTime = dateTimeInString.split("[.].*")[0] + "Z";
            } else if (dateTimeInString.contains("T") && dateTimeInString.contains("Z")) {
                LOG.info("Date time format \"{}\". Do not need to convert!", dateTimeInString);
            } else {
                LOG.info("Date time format \"{}\" is not correct. Convert to instant...", dateTimeInString);
                gmtDateTime = dateTimeInString.split("[.].*")[0] + "T00:00:00Z";
            }
            LOG.info("Instant date time format is: {}", gmtDateTime);
            return gmtDateTime;
        }
    }

    public void refreshCurrentTabInSalesforce() {
        waitForSpinnerSF(2);
        String currentTabLocator = "//li[contains(@class,'slds-is-active active')]//button[contains(@title,'Action')]";
        String refreshButton = "//a[./span[text()='Refresh Tab']]";
        clickOrEvaluateAndClick(currentTabLocator);
        clickOrEvaluateAndClick(refreshButton);
        LOG.info("Refreshed current tab");
    }

    public void goOnePageBack() {
        threadLocalDriverBasePage.get().navigate().back();
        waitForPageLoaded();
    }

    public void hoverOnElement(String locatorName, WebElement we) {
        waitForPageLoaded();
        Actions action = new Actions(threadLocalDriverBasePage.get());
        scrollElementToCenter(we);
        action.moveToElement(we).build().perform();
        waitForPageLoaded();
        LOG.info("Hovering over \"{}\" link ", locatorName);
    }

    public Map<String, Map<String, String>> verifyItemHasCorrectPrices(List<Map<String, String>> itemRows) {
        Map<String, Map<String, String>> actualCustomerItemsPrice = new HashMap<>();
        for (Map<String, String> itemRow : itemRows) {
            Map<String, String> itemInfo = new HashMap<>(itemRow);
            String itemName = TestDataLoader.getTestData(itemInfo.remove("item"));
            try {
                waitForVisibilityOfElementLocated(By.xpath("//*[text()='Monate 1 - 6']"), 5);
                TestDataLoader.setTestData("isFirst6MonthsPriceColumnPresent", "True");
            } catch (TimeoutException e) {
                TestDataLoader.setTestData("isFirst6MonthsPriceColumnPresent", "False");
            }
            Map<String, Map<String, String>> itemPrice = verifyPrices(itemInfo, itemName);
            actualCustomerItemsPrice.putAll(itemPrice);
        }
        return actualCustomerItemsPrice;
    }

    public Map<String, Map<String, String>> verifyPrices(Map<String, String> itemRow, String itemName) {
        LOG.info("----- Verifying price of \"{}\" -----", itemName);
        Map<String, Map<String, String>> actualItemPriceMap = new HashMap<>();
        Map<String, String> actualItemPriceInfo = new HashMap<>();
        boolean itemPriceCheckFail = false;
        for (Map.Entry<String, String> itemInfo : itemRow.entrySet()) {
            String infoKey = itemInfo.getKey().toLowerCase();
            String infoValue = itemInfo.getValue() == null ? "" : TestDataLoader.getTestData(itemInfo.getValue());
            LOG.info("\"{}\" --- {}", itemName, infoKey);
            // This status key is used to set background color for cell in FilesUtils.saveCustomerAndPriceInfoOfScreenIntoSheet()
            String infoKeyStatusKey = infoKey + "---status";
            String xpathToItemElement = getItemElementInPriceSection(infoKey, itemName);
            if (infoValue.isEmpty()) {
                waitForInvisibilityOfElementLocated(By.xpath(xpathToItemElement), 5);
                LOG.info("There's no {} of {} as expected", infoKey, itemName);
                actualItemPriceInfo.put(infoKey, "");
                actualItemPriceMap.put(itemName, actualItemPriceInfo);
            } else {
                if (infoKey.contains("origin")) {
                    String strikeThroughStyle = waitForVisibilityOfElementLocated(By.xpath(xpathToItemElement))
                            .getCssValue("text-decoration");
                    Assert.assertTrue(
                            String.format("Actual %s of %s is not strike-through", infoKey, itemName),
                            strikeThroughStyle.contains("line-through"));
                    LOG.info("The actual {} of {} is strike-through", infoKey, itemName);
                }
                String actualValue;
                try {
                    actualValue = getTextOfElement(By.xpath(xpathToItemElement)).trim();
                } catch (TimeoutException e) {
                    actualValue = "Display wrong name";
                }
                // Save customer and package prices in map to write excel file
                actualItemPriceInfo.put(infoKey, actualValue);
                actualItemPriceMap.put(itemName, actualItemPriceInfo);
                try {
                    actualItemPriceInfo.put("status", "FAILED");
                    actualItemPriceInfo.put(infoKeyStatusKey, "FAILED");
                    actualItemPriceMap.put(itemName, actualItemPriceInfo);
                    Assert.assertEquals(infoValue, actualValue);
                    actualItemPriceInfo.put(infoKeyStatusKey, "PASSED");
                    actualItemPriceMap.put(itemName, actualItemPriceInfo);
                    // If a type of price is failed, then mark whole bundle price as failure
                    if (!itemPriceCheckFail) {
                        actualItemPriceInfo.put("status", "PASSED");
                        actualItemPriceMap.put(itemName, actualItemPriceInfo);
                    }
                    LOG.info("Actual {} of {} is same as expected", infoKey, itemName);
                } catch (AssertionError error) {
                    if (ScenarioContext.getInstance()
                            .getScenario()
                            .getSourceTagNames()
                            .contains("@PriceCheckViaExcelFile")) {
                        itemPriceCheckFail = true;
                        LOG.info(
                                "Due to price check scenario generates output file, then do not throw assertion error");
                    } else {
                        throw error;
                    }
                }
            }
        }
        return actualItemPriceMap;
    }

    private String getItemElementInPriceSection(String info, String itemName) {
        // define general xpath to item info
        Map<String, String> generalItemXpathList = new HashMap<>();
        generalItemXpathList.put(
                "origin in-contract 1-6 months price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')])[1]");
        generalItemXpathList.put(
                "in-contract 1-6 months price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[@class='c-text-body'])[1]");
        generalItemXpathList.put(
                "origin in-contract price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')])[2]");
        generalItemXpathList.put(
                "in-contract price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[@class='c-text-body'])[2]");
        generalItemXpathList.put(
                "origin out-of-contract price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')])[3]");
        generalItemXpathList.put(
                "out-of-contract price",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//*[self::span[@class='c-text-body'] or self::div[@data-testid='text']])[3]");
        generalItemXpathList.put(
                "origin in-contract price (no 6 months price column)",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')])[1]");
        generalItemXpathList.put(
                "in-contract price (no 6 months price column)",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[@class='c-text-body'])[1]");
        generalItemXpathList.put(
                "origin out-of-contract price (no 6 months price column)",
                "(//section//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')])[2]");
        generalItemXpathList.put(
                "out-of-contract price (no 6 months price column)",
                "(//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//*[self::span[@class='c-text-body'] or self::div[@data-testid='text']])[2]");
        generalItemXpathList.put(
                "item description",
                "//tr[.//text()='%s']//following-sibling::tr[2]//div[@data-testid='text' and not(contains(@class,'text-bold'))]");
        generalItemXpathList.put(
                "origin one-time price",
                "//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[contains(@class,'smallprint')]");
        generalItemXpathList.put(
                "one-time price",
                "//tr[contains(normalize-space(),'%s')]/following-sibling::tr[1]//span[@class='c-text-body']");
        generalItemXpathList.put(
                "origin one-time price servicepauschale",
                "//tr[.//text()='%s']/following-sibling::tr[contains(normalize-space(),'Servicepauschale')]//span[contains(@class,'c-text-smallprint')]");
        generalItemXpathList.put(
                "one-time price servicepauschale",
                "//tr[.//text()='%s']/following-sibling::tr[contains(normalize-space(),'Servicepauschale')]//span[@class='c-text-body']");
        generalItemXpathList.put(
                "item description servicepauschale",
                "//tr[.//text()='%s']/following-sibling::tr[contains(normalize-space(),'Servicepauschale')]//following-sibling::tr[1]//div[@data-testid='text' and not(contains(@class,'text-bold'))]");
        generalItemXpathList.put(
                "upsell status",
                "//tr[contains(normalize-space(),'%s')]/preceding-sibling::tr[1]//div[contains(@data-testid,'text') and not(contains(@class,'u-margin-x-tiny'))]");
        generalItemXpathList.put(
                "section",
                "(//tr[contains(normalize-space(),'%s')]/preceding-sibling::tr//div[contains(@class,'u-margin-x-tiny')])[last()]");
        String isFirst6MonthsPriceColumnPresent = TestDataLoader.getTestData("@TD:isFirst6MonthsPriceColumnPresent");
        String xpathToItemElement;
        if (info.contains("price") && !info.contains("one-time"))
            xpathToItemElement = isFirst6MonthsPriceColumnPresent.equals("False")
                    ? String.format(generalItemXpathList.get(info + " (no 6 months price column)"), itemName)
                    : String.format(generalItemXpathList.get(info), itemName);
        else xpathToItemElement = String.format(generalItemXpathList.get(info), itemName);
        if (itemName.contains("Servicepauschale")) {
            itemName = itemName.split(" Servicepauschale")[0];
            xpathToItemElement = String.format(generalItemXpathList.get(info + " servicepauschale"), itemName);
        }
        if (itemName.contains("total ")) {
            String displayedItemName = itemName.equals("total monthly price")
                    ? "Monat\u00ADliche\u00A0Gesamt\u00ADkosten"
                    : "Einmalige Gesamtkosten";
            xpathToItemElement = xpathToItemElement
                    .replace(itemName, displayedItemName)
                    .replace("span[@class='c-text-body']", "div[@data-testid='text']");
        }
        LOG.info("Locator: {}", xpathToItemElement);
        return xpathToItemElement;
    }

    public List<String> convertToTestDataWithNoSpecialChar(List<String> contentList) {
        List<String> newContentList = new ArrayList<>();
        for (String value : contentList) {
            getStringNoSpecialChar(newContentList, value);
        }
        return newContentList;
    }

    public void getStringNoSpecialChar(List<String> newContentList, String value) {
        if (value.startsWith("@TD") || value.startsWith("@date") || value.startsWith("@GermanDate")) {
            String emailContent = TestDataLoader.getTestData(value);
            if (emailContent.contains("|")) {
                String[] expectedText = emailContent.split(" \\| ");
                newContentList.addAll(Arrays.asList(expectedText));
            } else newContentList.add(emailContent);
        } else newContentList.add(value);
    }

    public void sortSalesforcetableColumn(String sortCriteria, String sortOrder) {
        // sort order must be "Descending" or "Ascending"
        Assert.assertTrue(
                "sortOrder must be {Descending,Ascending}",
                sortOrder.equals("Descending") || sortOrder.equals("Ascending"));
        String sortXpath = String.format(
                "//span[@title='%s']/ancestor::a//following-sibling::span[@aria-live='assertive']", sortCriteria);
        String columnNameLocator = String.format("(//span[@title='%s'])[last()]", sortCriteria);
        String currentSortOrder =
                waitForVisibilityOfElementLocated(By.xpath(sortXpath)).getText();
        if (currentSortOrder.isEmpty()) {
            clickOrEvaluateAndClick(columnNameLocator);
            waitForSpinnerSF(1);
            currentSortOrder =
                    waitForVisibilityOfElementLocated(By.xpath(sortXpath)).getText();
        }
        LOG.info("Current sort Order is : {}", currentSortOrder);
        if (!currentSortOrder.contains(sortOrder)) {
            clickOrEvaluateAndClick(columnNameLocator);
            waitForSpinnerSF(1);
            LOG.info("Sorted {} as {} successfully", sortCriteria, sortOrder);
        }
    }

    public void verifyLengthOfElementText(String elementName, By locator, String length) {
        String elementText = getTextOfElement(locator).trim();
        int actualElementTextLength = elementText.length();
        TestDataLoader.setTestData(elementName.replace(" ", "_"), elementText);
        Assert.assertEquals(Integer.parseInt(length), actualElementTextLength);
        LOG.info(
                "Element \"{}\" has text length \"{}\" as expected \"{}\"",
                elementName,
                actualElementTextLength,
                length);
    }

    public static String getValueMatchRegexFromString(String regex, String string) {
        String matchingValue = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            matchingValue = matcher.group(0);
            if (!matchingValue.isEmpty()) {
                break;
            }
        }
        LOG.info("The value matching with the regex '{}' from string '{}' is:\n'{}'", regex, string, matchingValue);
        return matchingValue;
    }

    public void selectValueFromDropDownList(String valueToSelect, String list, String dropdownListXpath) {
        valueToSelect = TestDataLoader.getTestData(valueToSelect);
        String selector =
                "//*[@role='presentation' or @role='menuitem' or @role='option']//*[text()='" + valueToSelect + "']";
        clickOrEvaluateAndClick(dropdownListXpath);
        clickOrEvaluateAndClick(selector);
        LOG.info("Selected value {} --- from dropdown list -----{}", valueToSelect, list);
    }

    public void verifyInformationHasTestDataLoader(String informationType, List<String> informationList) {
        String keyword;
        String value;
        List<String> expectedResult = new ArrayList<>();
        for (String data : informationList) {
            String newData;
            if (data.contains("@TD:")) {
                keyword = getValueMatchRegexFromString("@TD:\\w+", data);
                value = TestDataLoader.getTestData(keyword);
                newData = data.replace(keyword, value);
                expectedResult.add(newData);
            } else expectedResult.add(data);
            waitForPageLoaded();
            scrollToBottom(200);
            String pageContent = getTextOfElement(By.xpath("//body[@class='desktop']"));
            LOG.info("Page content is {}", pageContent);
            expectedResult.forEach(info -> {
                Assert.assertTrue(pageContent.contains(info));
                LOG.info("This \"{}\" {} displays as expected", info, informationType);
            });
        }
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

    public void verifyInformationNotDisplay(String informationType, List<String> informationList) {
        informationList = convertToTestDataWithNoSpecialChar(informationList);
        waitForPageLoaded();
        scrollToBottom();
        String pageContent = getTextOfElement(By.xpath("//body"));
        LOG.info("Page content is {}", pageContent);
        informationList.forEach(info -> {
            Assert.assertFalse(pageContent.contains(info));
            LOG.info("This \"{}\" {} does not displays as expected", info, informationType);
        });
    }

    public void verifyInformationNotDisplayedInSFTab(String informationType, List<String> informationList) {
        informationList = convertToTestDataWithNoSpecialChar(informationList);
        waitForPageLoaded();
        scrollToBottom();
        String pageContent = getTextOfElement(By.xpath("//div[contains(@class,'active lafPageHost')]"));
        LOG.info("Page content is {}", pageContent);
        informationList.forEach(info -> {
            Assert.assertFalse(String.format("%s %s is displayed!", informationType, info), pageContent.contains(info));
            LOG.info("This \"{}\" {} does not displays as expected", info, informationType);
        });
    }

    public void verifyInformationDisplayedInSFTab(String informationType, List<String> informationList) {
        informationList = convertToTestDataWithNoSpecialChar(informationList);
        waitForSpinnerSF(1);
        waitForPageLoaded();
        String pageContent;
        if (informationType.equalsIgnoreCase("email content")) {
            threadLocalDriverBasePage
                    .get()
                    .switchTo()
                    .frame(waitForVisibilityOfElementLocated(By.xpath("//iframe[@title='CK Editor Container']")));
            threadLocalDriverBasePage
                    .get()
                    .switchTo()
                    .frame(waitForVisibilityOfElementLocated(By.xpath("//iframe[@title='Email Body']")));
            pageContent = getTextOfElement(By.xpath("//body[@contenteditable='true']"));
            threadLocalDriverBasePage.get().switchTo().defaultContent();
        } else pageContent = getTextOfElement(By.xpath("(//div[contains(@class,' active lafPageHost')])[last()]"));
        LOG.info("Page content is {}", pageContent);
        for (String info : informationList) {
            try {
                Assert.assertTrue(pageContent.contains(info));
            } catch (AssertionError e) {
                LOG.info(
                        "This \"{}\" {} does not display as expected. Scroll to bottom and verify again",
                        info,
                        informationType);
                scrollToBottom();
                pageContent = getTextOfElement(By.xpath("//div[contains(@class,'active lafPageHost')]"));
                Assert.assertTrue(
                        String.format("%s %s is not displayed!", informationType, info), pageContent.contains(info));
            }
            LOG.info("This \"{}\" {} displays as expected", info, informationType);
        }
    }

    public static String getFacebookUserId(String username) {
        String userId = null;
        if (testedEnv.equalsIgnoreCase("sit")) {
            userId = "805329019831936";
        }
        if (testedEnv.equalsIgnoreCase("prod")) {
            userId = "109554844183465";
        }
        if (testedEnv.equalsIgnoreCase("uat")) {
            if (username.contains("SkyService")) userId = TestDataLoader.getTestData("TD:chatBotId_SkyService");
            else userId = TestDataLoader.getTestData("TD:chatBotId_NonSkyService");
        }
        Assert.assertNotNull(
                String.format("Cannot get user id of the %s in %s environment", username, testedEnv), userId);
        return userId;
    }

    public void clickElementUntilItNotDisplay(String elementXpath, int retry) {
        int attempt = 0;
        do {
            clickOrEvaluateAndClick(elementXpath);
            try {
                waitForInvisibilityOfElementLocated(By.xpath(elementXpath), 10);
                LOG.info("Element {} is clicked successfully and not display", elementXpath);
                break;
            } catch (Exception e) {
                attempt++;
            }
        } while (attempt < retry);
    }

    public void createCtiCaseForCustomer(String accountId, String callReason) {
        String customerID = TestDataLoader.getTestData(accountId);
        String callId = generateRandomWord(13);
        String ctiUrl = "https://skyde--uat--c.visualforce.com/flow/CTI_Flow_For_Softphone_Integration?extID="
                + customerID + "&reasonForCall=" + callReason + "&CallID=" + callId;
        threadLocalDriverBasePage.get().get(ctiUrl);
        waitForPageLoaded();
        waitForSpinnerCDP(1);
        LOG.info("Navigate to create CTI case url: {}", ctiUrl);
        clickButtonByText("Next");
        waitForSpinnerCDP(1);
        verifyVisibilityOfElement("Case detail", "//*[contains(@class,'entityNameTitle') and text()='Case']");
        LOG.info("Case detail page is displayed");
    }

    public void verifyPageTitle(String pageTitle) {
        Assert.assertTrue(
                String.format(
                        "The title [%s] is not matched with current: [%s]",
                        pageTitle, threadLocalDriverBasePage.get().getTitle()),
                waitForPageTitleToContain(pageTitle, 30));
        LOG.info("\"{}\" page title is verified", pageTitle);
    }

    public void switchToTabX(Integer tab) {
        ArrayList<String> tabs =
                new ArrayList<>(threadLocalDriverBasePage.get().getWindowHandles());
        for (String singleTab : tabs) {
            String title = threadLocalDriverBasePage
                    .get()
                    .switchTo()
                    .window(singleTab)
                    .getTitle();
            LOG.info(title);
        }
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(tab));

    }

    public void verifyElementIsClickable(String element, String elementXpath) {
        WebElement webElement;
        try {
            webElement = waitForElementToBeClickable(By.xpath(elementXpath), 20);
            LOG.info("The element {} is clickable", element);
        } catch (TimeoutException e) {
            LOG.info("Element {} not visible. Scroll to bottom and verify again", element);
            scrollToBottom(200);
            webElement = waitForElementToBeClickable(By.xpath(elementXpath), 10);
            LOG.info("The element {} is clickable", element);
        } catch (Exception e) {
            throw new CucumberException("Can not find the element after 30 seconds");
        }
        scrollToViaAction(webElement);
    }

    public String getStatusInGerman(String status) {
        status = status.equalsIgnoreCase("selected") ? "Ausgewählt" : "Auswählen";
        return status;
    }
    public void checkIfCheckBoxIsClickable(String action, String checkboxXpath) {
        if (action.equals("can not")) {
            try {
                waitForElementToBeClickable(By.xpath(checkboxXpath), 2).click();
                throw new CucumberException("Fail due to checkbox " + checkboxXpath + " is clickable by the user.");
            } catch (TimeoutException | ElementClickInterceptedException e) {
                LOG.info("Checkbox {} is not clickable as expected", checkboxXpath);
            }
        } else if (action.equals("can")) {
            waitForElementToBeClickable(By.xpath(checkboxXpath), 2);
            LOG.info("Checkbox {} is clickable as expected", checkboxXpath);
        } else throw new CucumberException("Action must be {can or cannot}");
    }

    public void verifyInformationNotDisplayInSpecificArea(
            String objectName,
            Map<String, String> areaXpath,
            List<String> informationOrElementXpathList,
            int scrollLoop) {
        Assert.assertEquals(
                "Area xpath map must contain 2 key-value {area scrollable xpath, area body xpath}",
                2,
                areaXpath.keySet().size());
        Assert.assertTrue(
                "Area xpath map must contain key {area scrollable xpath",
                areaXpath.containsKey("area scrollable xpath"));
        Assert.assertTrue(
                "Area xpath map must contain key {area body xpath}", areaXpath.containsKey("area body xpath"));
        String areaScrollableXpath = areaXpath.get("area scrollable xpath");
        String areaBodyXpath = areaXpath.get("area body xpath");
        // Check if area has scroller or not
        try {
            waitForVisibilityOfElementLocated(By.xpath(areaScrollableXpath), 2);
            scrollInsideElement(areaScrollableXpath)
                    .to(Directions.BOTTOM)
                    .withLoop(scrollLoop)
                    .perform();
        } catch (TimeoutException ex) {
            LOG.info("The area does not have scroller. Continue...");
        }
        String conversationContent = getTextOfElement(By.xpath(areaBodyXpath));
        informationOrElementXpathList = convertToTestDataWithNoSpecialChar(informationOrElementXpathList);
        if (objectName.contains("information")) {
            informationOrElementXpathList.forEach(element -> {
                Assert.assertFalse(
                        "Content in area located by " + areaBodyXpath + " contain information " + element,
                        conversationContent.contains(element));
                LOG.info("Content in area {} does not contain information {} as expectation", areaBodyXpath, element);
            });
        } else
            informationOrElementXpathList.forEach(element -> {
                waitForInvisibilityOfElementLocated(By.xpath(element), 15);
                LOG.info(
                        "Area located by \"{}\" not have \"{}\" \"{}\" as expectation",
                        areaBodyXpath,
                        objectName,
                        element);
            });
    }

    public void waitForElementToBeSelected(By by, boolean isSelected, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        wait.until(ExpectedConditions.elementSelectionStateToBe(by, isSelected));
    }

    public boolean waitForPageTitleToContain(String title, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.titleContains(title));
    }

    public List<WebElement> waitForNumberOfElementsToBePresent(By by, int numberOfElements, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.numberOfElementsToBe(by, numberOfElements));
    }

    public boolean waiForTheUrlToContain(String url, int timeout) {
        WebDriverWait wait = new WebDriverWait(threadLocalDriverBasePage.get(), Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.urlContains(url));
    }

    public void clickElementAfterVisibility(String elementLocator) {
        LOG.info("Element locator is {}", elementLocator);
        WebElement element = waitForVisibilityOfElementLocated(By.xpath(elementLocator));
        scrollElementToCenter(element);
        try {
            element.click();
        } catch (Exception | AssertionError e) {
            LOG.info("Found exception: {}", e.getMessage());
            executeJs("arguments[0].click();", element);
        }
        LOG.info("Clicked on evaluated xpath: \"{}\".", elementLocator);
    }

    public void ensureCheckBoxIsInStatus(String elementXpath, String status) {
        if (!status.equalsIgnoreCase("selected") && !status.equalsIgnoreCase("not selected")) {
            throw new CucumberException(
                    String.format("%s Invalid checkbox status: %s", CURRENT_THREAD_SCENARIO.get(), status));
        }
        boolean currentStatusIsSelected =
                waitForVisibilityOfElementLocated(By.xpath(elementXpath)).isSelected();
        boolean expectStatusIsSelected = status.equalsIgnoreCase("selected");
        int attempt = 0;
        while (currentStatusIsSelected != expectStatusIsSelected) {
            if (attempt > 10) {
                throw new CucumberException(String.format(
                        "%s Fail to click on checkbox locator %s", CURRENT_THREAD_SCENARIO.get(), elementXpath));
            }
            waitForVisibilityOfElementLocated(By.xpath(elementXpath + "/following-sibling::*"))
                    .click();
            currentStatusIsSelected =
                    waitForVisibilityOfElementLocated(By.xpath(elementXpath)).isSelected();
            attempt++;
        }
    }

    public WebElement verifyVisibilityOfElements(String packageName, String subscriptionValue, String elementXpath) {
        waitForPageLoaded();
        WebElement webElement;
        webElement = waitForVisibilityOfElementLocated(By.xpath(elementXpath), 30);
        LOG.info("{} has expected subscription value - {}", packageName, subscriptionValue);
        scrollTo(webElement);
        return webElement;
    }
}
