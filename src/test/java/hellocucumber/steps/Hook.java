package hellocucumber.steps;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import hellocucumber.utilities.BasePage;
import hellocucumber.utilities.DriverUtil;
import hellocucumber.utilities.ScenarioContext;
import hellocucumber.utilities.TestDataLoader;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.PickleStepTestStep;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static hellocucumber.utilities.DriverUtil.threadLocalActiveBrowsers;

public class Hook {

    private static final Logger LOG = LogManager.getLogger(Hook.class);
    public static String browser = System.getProperty("browser");
    public static String testedEnv = System.getProperty("testedEnv");
    public static String platform = System.getProperty("platform");
    public static String executingEnv = System.getProperty("executingEnv");
    public static String stepStartTime = "";
    public static ThreadLocal<String> scenarioStartTime = new ThreadLocal<>();
    public static ThreadLocal<Date> testStartDateTime = new ThreadLocal<>();
    public static List<String> failedScenarios = new ArrayList<>();
    public static List<String> passedScenarios = new ArrayList<>();
    public static ThreadLocal<Boolean> threadLocalCookieAccepted = new ThreadLocal<>();
    private static boolean SKIP_TEST_ON_ERROR = false;
    public static final ThreadLocal<Map<String, String>> threadLocalDataSetInExecution = new ThreadLocal<>();
    public static List<Scenario> listOfScenariosInExecution = new ArrayList<>();
    public static final ThreadLocal<Integer> threadLocalCurrentStepNumber = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, List<String>>> captureInfo = new ThreadLocal<>();
    private static final ThreadLocal<List<PickleStepTestStep>> testStepTitles = new ThreadLocal<>();


    public static void saveStartTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        stepStartTime = dtf.format(now);
    }

    @Before
    public static synchronized void beginScenario(Scenario scenario)
            throws NoSuchFieldException, IllegalAccessException {
        Collection<String> tags = scenario.getSourceTagNames();
        ScenarioContext.getInstance().setScenario(scenario);
        testStepTitles.set(ScenarioContext.getInstance().getSteps());
        LOG.info("-------------------------------------------");
        LOG.info("Running Tests {} in \"{}\" browser", scenario.getName(), browser);
        LocalDateTime now = LocalDateTime.now();
        scenarioStartTime.set(now.toString());
        // Set date to verify incoming mails after this time
        testStartDateTime.set(new Date());
        threadLocalCookieAccepted.set(false);
        new TestDataLoader();
        TestDataLoader.setTestData("TaggingOfScenario", getTaggingOfScenario(tags));
        // initialize the Map<String, RemoteWebDriver> for storing all active browsers sessions
        threadLocalActiveBrowsers.set(new HashMap<>());
        RemoteWebDriver driver = DriverUtil.initDriver(browser, false, false);
        if (browser.contains("GCP") || browser.contains("Headless")) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }
        driver.manage().deleteAllCookies();
        LOG.info("START SCENARIO '{}'", scenario.getName());
        LOG.info("With Tags: {}", tags);
        LOG.info("-------------------------------------------");
        threadLocalCurrentStepNumber.set(0);
        BasePage.CURRENT_THREAD_SCENARIO.set(scenario.getName() + scenario.getSourceTagNames() + " --- ");
        captureInfo.set(readJsonFileToList("CAPTURE.json"));
    }



    @After(order = 1)
    public static synchronized void endScenario(Scenario scenario) {
        LOG.info("-------------------------------------------");
        String scenarioName = scenario.getName().toUpperCase();
        LOG.info("END SCENARIO {}", scenarioName);
        LOG.info("-------------------------------------------");
        Collection<String> scenarioTags = scenario.getSourceTagNames();

        if (scenario.isFailed()) {
            failedScenarios.add(scenarioTags.toString());
            LOG.info("Scenario with the following tags '{}' failed", scenarioTags);
            if (DriverUtil.getDriver() != null) {
                captureFullScreenShot(scenario);
            }
        } else if (SKIP_TEST_ON_ERROR) {
            failedScenarios.add(scenarioTags.toString());
            LOG.info(
                    "Scenario '{}' with the following tags '{}' is skipped due to previous Scenario failed / is aborted.",
                    scenarioName,
                    scenarioTags);
        } else {
            passedScenarios.add(scenario.getSourceTagNames().toString());
            LOG.info("Scenario with the following tags '{}' passed", scenarioTags);
        }
        // Remove scenario context thread value after scenario
        ScenarioContext.getInstance().unload();
        testStepTitles.remove();
        captureInfo.remove();
        BasePage.CURRENT_THREAD_SCENARIO.remove();
        LOG.info("-------------------------------------------");
    }


    public static Map<String, List<String>> readJsonFileToList(String fileName) {
        List<String> listTagsFinal = new ArrayList<>();
        List<String> listStepsFinal = new ArrayList<>();
        Map<String, List<String>> map = new HashMap<>();
        JSONArray listKeyword = null;
        String res = null;
        try {
            res = FileUtils.readFileToString(
                    new File("src/main/java/testdata/input/" + fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CucumberException("Can't read Json file: ", e);
        }
        JSONArray arr = new JSONArray(res);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject block = arr.getJSONObject(i);
            listKeyword = block.getJSONArray("Tags");
            for (int index = 0; index < listKeyword.length(); index++) {
                listTagsFinal.add(listKeyword.getString(index));
            }
            listKeyword = block.getJSONArray("Steps");
            for (int index = 0; index < listKeyword.length(); index++) {
                listStepsFinal.add(listKeyword.getString(index));
            }
            for (String item : listTagsFinal) {
                map.put(item, listStepsFinal);
            }
        }
        LOG.info("Read Json file [{}] is: {}", fileName, map);
        return map;
    }

    public static void captureFullScreenShot(Scenario message) {
        byte[] screenshot;
        try {
            screenshot =
                    Shutterbug.shootPage(DriverUtil.getDriver(), Capture.FULL).getBytes();
            Date current = new Date();
            message.attach(screenshot, "image/png", message.getName().replace(" ", "_") + current);
        } catch (IOException e) {
            LOG.info("Can not capture full screenshot: ", e);
        }
    }

    private static String getTaggingOfScenario(Collection<String> tags) {
        String prefix = testedEnv.equals("dev") ? "@E2ED-" : "@TEST-";
        for (String tag : tags) {
            if (tag.contains(prefix)) {
                return tag.replace("@", "");
            }
        }
        return new ArrayList<>(tags).get(0);
    }

}
