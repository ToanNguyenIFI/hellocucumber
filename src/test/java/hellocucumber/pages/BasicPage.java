package hellocucumber.pages;

import hellocucumber.locators.BasicPageLocators;
import hellocucumber.utilities.BasePage;
import hellocucumber.utilities.DriverUtil;
import hellocucumber.utilities.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class BasicPage extends BasePage {
    public BasicPage(RemoteWebDriver driver) {
        super(driver);
    }

    private static final Logger LOG = LogManager.getLogger(BasicPage.class);
    private final Map<String, String> xpathToPage = BasicPageLocators.createLibraryPage();
    private final Map<String, String> xpathToElement = BasicPageLocators.createLibraryElement();
    public static ThreadLocal<Boolean> safariAuthorization = new ThreadLocal<>();
    public static ThreadLocal<List<String>> email_list = ThreadLocal.withInitial(ArrayList::new),
      contact_id_list = ThreadLocal.withInitial(ArrayList::new), customer_id_list = ThreadLocal.withInitial(ArrayList::new);
    private static final Object fileWriteLock = new Object();
    private static boolean isFileDeleted = false;
    public static ThreadLocal<Boolean> isActivationFailedForAnyCustomer = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public void openURL(String url) {
        String extractedUrl = TestDataLoader.getTestData(url);
            int maxRetries = 3; // Number of retry attempts
            int attempt = 0;
            boolean success = false;
            while (attempt < maxRetries && !success) {
                try {
                    attempt++;
                    DriverUtil.threadLocalActiveBrowsers.get().get("current").get(extractedUrl);
                    success = true; // If the page loads successfully, exit the loop
                } catch (TimeoutException e) {
                    LOG.warn("Attempt {} to load URL {} failed. Retrying...", attempt, extractedUrl);
                    if (attempt == maxRetries) {
                        LOG.error("Failed to load URL {} after {} attempts", extractedUrl, maxRetries);
                        // Rethrow the exception if max retries are reached
                        throw new CucumberException(
                                "Failed to load URL " + extractedUrl + "after " + maxRetries + " attempts");
                    }
                }
            }
    }

    public List<String> getTabs() {
        List<String> tabs = new ArrayList<>(threadLocalDriverBasePage.get().getWindowHandles());
        tabs.forEach(tab -> LOG.info("Active tab is: {}", tab));
        return tabs;
    }

    public void switchToTabByIndex(Integer idx) {
        waitFor(1).seconds();
        List<String> tabs = getTabs();
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(idx));
        LOG.info(threadLocalDriverBasePage.get().manage().window().getPosition());
    }

    public void switchToTabByIndexAndCLoseByIndex(Integer expectedHandleTab, Integer expectedClosedTab) {
        List<String> tabs = getTabs();
        LOG.info("{} tabs open", tabs.size());
        threadLocalDriverBasePage
                .get()
                .switchTo()
                .window(tabs.get(expectedClosedTab))
                .close();
        LOG.info("Closed tab {}", tabs.get(expectedClosedTab));
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(expectedHandleTab));
        LOG.info("Switch to tab {}", tabs.get(expectedHandleTab));
        LOG.info(threadLocalDriverBasePage.get().manage().window().getPosition());
    }

    public void switchToFirstTab() {
        ArrayList<String> tabs = new ArrayList<>(threadLocalDriverBasePage.get().getWindowHandles());
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(0));
    }

    public void switchToFirstTabAndClose() {
        ArrayList<String> tabs = new ArrayList<>(threadLocalDriverBasePage.get().getWindowHandles());
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(1)).close();
        threadLocalDriverBasePage.get().switchTo().window(tabs.get(0));
    }

    public static String[] splitStringIntoArray(String string) {
        return splitStringIntoArray(string, "|");
    }

    public static String[] splitStringIntoArray(String string, String separator) {
        return string.split(String.format("[\\%s]", separator));
    }

    public void switchTab(String tab) {
        tab = TestDataLoader.getTestData(tab);
        clickOrEvaluateAndClick(String.format("//a[@title='%s' and contains(@class,'label-action')]", tab));
    }


    public void currentPageUrlContains(String urlPart) {
        waitForPageLoaded();
        Assert.assertTrue(waiForTheUrlToContain(urlPart, 5));
        LOG.info("Url contains <{}> as expected.", urlPart);
    }

    public void closeCurrentPage() {
        threadLocalDriverBasePage.get().close();
    }

    // Save the key-value in test data runtime
    public void saveKeyWithValue(String key, String value) {
        if (key.contains("SecondCommitmentStartDate") || key.contains("SecondCommitmentEndDate")) {
            String[] dateValue = value.split("_into_");
            value = TestDataLoader.getTestData(
                    "@date:" + TestDataLoader.getTestData(dateValue[1]).substring(0, 10) + "_ofFORMATyyyy-MM-dd/"
                            + dateValue[0] + "_FORMATyyyy-MM-dd");
            value += TestDataLoader.getTestData(dateValue[1]).substring(10);
        }
        if (value.contains(" ")
                && (value.toLowerCase().contains("@td:") || value.toLowerCase().contains("@date:"))) {
            StringBuilder valueBuilder = new StringBuilder();
            String[] valueArray = value.split(" ");
            for (String val : valueArray) {
                valueBuilder.append(TestDataLoader.getTestData(val)).append(" ");
            }
            value = valueBuilder.toString().strip();
        }
        if ("Trensport_cancellation_period_mein_abo".equals(key)
                || "Trensport_end_of_commitment_mein_abo".equals(key)) {
            String[] dateValue = TestDataLoader.getTestData(value).split("-");
            value = String.format("%s.%s.%s", dateValue[2], dateValue[1], dateValue[0]);
        }

        if (key.contains("Migration_First_CommitmentStartDate")
                || key.contains("Migration_First_CommitmentEndDate")
                || key.contains("Migration_Second_CommitmentStartDate")) {
            String[] dateValue = value.split("_into_");
            value = TestDataLoader.getTestData(
                    "@date:" + TestDataLoader.getTestData(dateValue[1]).substring(0, 10) + "_ofFORMATyyyy-MM-dd/"
                            + dateValue[0] + "_FORMATyyyy-MM-dd");
            value += TestDataLoader.getTestData(dateValue[1]).substring(10);
            dateValue = value.split("T");
            String newDateValue[] = dateValue[0].split("-");
            value = String.format("%s.%s.%s", newDateValue[2], newDateValue[1], newDateValue[0]);
        }
        if (key.contains("Migration_FirstContract_CommitementStartDate") || key.contains("Migration_FirstContract_CommitmentEndDate")
                || key.contains("Migration_SecondContract_CommitmentStartDate")) {
            value = TestDataLoader.getTestData(value);
            String[] newDateValue = value.split("\\.");
            value = String.format("%s-%s-%s", newDateValue[2], newDateValue[1], newDateValue[0]);
        }
        TestDataLoader.setTestData(key, TestDataLoader.getTestData(value));
    }



    public void verifyInformation(String dataType, List<String> expectedInfo) {
        if (dataType.equals("information") || dataType.contains("section"))
            verifyInformationDisplay(dataType, expectedInfo);
    }


    public Map<String, Integer> findPaddingValue(List<Map<String, String>> uiData) {
        Map<String, Integer> padMap = new LinkedHashMap<>();
        for (String key : uiData.get(0).keySet()) {
            padMap.put(key, key.length());
        }
        for (Map<String, String> mapData : uiData) {
            for (Map.Entry<String, String> rowValue : mapData.entrySet()) {
                String colKey = rowValue.getKey();
                String colValue = rowValue.getValue();
                padMap.put(colKey, colValue.length() > padMap.get(colKey) ? colValue.length() : padMap.get(colKey));
            }
        }
        return padMap;
    }

    public void verifyElement(String element) {
        try {
            waitForVisibilityOfElementLocated(By.xpath(xpathToElement.get(element)), 30);
            LOG.info("Expected element displayed");
        } catch (Exception e) {
            LOG.info("Expected element not displayed, try to scroll to bottom and verify again");
            scrollToBottom(100);
            waitForVisibilityOfElementLocated(By.xpath(xpathToElement.get(element)), 30);
            LOG.info("Expected element displayed");
        }
    }

    public void clickOnElement(String button) {
        if ("live chat".equals(button)) {
            clickElementUntilItNotDisplay(xpathToElement.get(button), 5);
            waitForVisibilityOfElementLocated(By.xpath("//div[contains(@class,'AgentStateChatInput')]//textarea"), 60);
        } else {
            clickOrEvaluateAndClick(xpathToElement.get(button));
        }
    }

    public void compareTwoStrings(String value1, String value2, String comparison) {
        value1 = TestDataLoader.getTestData(value1);
        value2 = TestDataLoader.getTestData(value2);
        LOG.info("Comparing the'{}' to '{}' '{}'", value1, comparison, value2);
        switch (comparison) {
            case "equals":
                Assert.assertEquals(value1, value2);
                break;
            case "contains":
                Assert.assertTrue(value1.contains(value2));
                break;
            case "does not contain":
                Assert.assertFalse(value1.contains(value2));
                break;
            default:
                throw new CucumberException(
                        "Invalid comparison. The valid comparison should be one of the following: {'equals', 'contains', 'does not contain'}");
        }
    }

    public void ensureFileExistenceWithDynamicNameAsExpectation(String fileName, String existence) {
        switch (existence.toLowerCase()) {
            case "not existing":
                ensureFileWithDynamicNameNotExist(fileName);
                break;
            case "existing":
                ensureFileWithDynamicNameExist(fileName);
                break;
            default:
                throw new CucumberException("Existence should be \"not existing\" or \"existing\"");
        }
    }

    private void ensureFileWithDynamicNameExist(String fileName) {
        String downloadFilepath = DriverUtil.PATH_TO_DOWNLOAD_DIR;
        File downloadDir = new File(downloadFilepath);
        String filePrefix = fileName.split(BasePage.REGEX_TO_FIND_LAST_DOT)[0].replace("*", "");
        String fileType = fileName.split(BasePage.REGEX_TO_FIND_LAST_DOT)[1];
        boolean exist = false;
        int tryCount = 0;
        do {
            File[] fileList = downloadDir.listFiles();
            Assert.assertNotNull(fileList, "Invalid download directory" + downloadFilepath);
            LOG.info("Downloaded files are: {}", Arrays.asList(fileList));
            try {
                for (File file : fileList) {
                    exist = file.getName().startsWith(filePrefix)
                            && file.getName().endsWith(fileType);
                    if (exist) break;
                }
                Assert.assertTrue(exist);
            } catch (AssertionError assertionError) {
                LOG.info("File {} does not exist. Try to verify again after 10 seconds...", fileName);
                tryCount++;
                waitFor(10).seconds();
            }
            if (exist) break;
            else LOG.info("File {} can be in downloading progress. Let's wait...", fileName);
        } while (tryCount < 30);
        if (exist) LOG.info("File {} exists. Download file successfully!", fileName);
        else throw new CucumberException("File " + fileName + " does not exist. Failed to download!");
    }

    private void ensureFileWithDynamicNameNotExist(String fileName) {
        String downloadFilepath = DriverUtil.PATH_TO_DOWNLOAD_DIR;
        File downloadDir = new File(downloadFilepath);
        if (!downloadDir.exists()) LOG.info("File {} not exists. Continue!", fileName);
        else {
            String filePrefix = fileName.split(BasePage.REGEX_TO_FIND_LAST_DOT)[0].replace("*", "");
            String fileType = fileName.split(BasePage.REGEX_TO_FIND_LAST_DOT)[1];
            File[] fileList = downloadDir.listFiles();
            Assert.assertNotNull(fileList, "Invalid download directory" + downloadFilepath);
            LOG.info("Downloaded files are: {}", Arrays.asList(fileList));
            boolean result = true;
            for (File file : fileList) {
                result = file.getName().startsWith(filePrefix) && file.getName().endsWith(fileType);
                if (result) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    LOG.info("File {} exists. File deleted successfully!", fileName);
                    break;
                }
            }
            if (!result) {
                LOG.info("File {} not exists. Continue!", fileName);
            }
        }
    }
}
