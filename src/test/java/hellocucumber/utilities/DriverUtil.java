package hellocucumber.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import hellocucumber.steps.Hook;
import io.cucumber.core.exception.CucumberException;
import org.apache.commons.exec.Executor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.openqa.selenium.chrome.ChromeOptions.LOGGING_PREFS;

public class DriverUtil {
    public static final String PATH_TO_DOWNLOAD_DIR =
            System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads"
                    + DriverUtil.FILE_SEPARATOR + TestDataLoader.getTestData("@TD:TaggingOfScenario");
    private static RemoteWebDriver driver;
    private static DesiredCapabilities dc;
    private static final Logger LOG = LogManager.getLogger(DriverUtil.class);
    public static final ThreadLocal<Map<String, RemoteWebDriver>> threadLocalActiveBrowsers = new ThreadLocal<>();
    private static final String APPIUM_SERVER_LOCAL_HOST = "127.0.0.1";
    private static String proxyHost;
    private static String proxyPort;
    private static final String HUB_ENDPOINT = System.getenv("HUB_ENDPOINT");
    public static ThreadLocal<RemoteWebDriver> threadLocalDriver = new ThreadLocal<>();
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    static Path modHeaderExtension = Paths.get(System.getProperty("user.dir") + FILE_SEPARATOR + "resources"
            + FILE_SEPARATOR + "chrome_extension" + FILE_SEPARATOR + "modheader.crx");
    static Path urlBlockerExtension = Paths.get(System.getProperty("user.dir") + FILE_SEPARATOR + "resources"
            + FILE_SEPARATOR + "chrome_extension" + FILE_SEPARATOR + "HTTP-Request-Blocker.crx");

    private DriverUtil() {
        threadLocalActiveBrowsers.set(new HashMap<>());
    }

    private static Executor executor;

    public static synchronized RemoteWebDriver getDriver() {
        return threadLocalActiveBrowsers.get().get("current");
    }

    /* LOCAL DRIVER */

    public static RemoteWebDriver initChrome(boolean incognito, boolean proxy) {
        RemoteWebDriver driver;
        LoggingPreferences logPrefsCHROME = new LoggingPreferences();
        logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setEnableDownloads(true);
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("profile.default_content_setting_values.notifications", 2);
        preferences.put("safebrowsing.enabled", "true");
        preferences.put("plugins.plugins_disabled", new String[] {"Chrome PDF Viewer"});
        preferences.put("plugins.always_open_pdf_externally", true);
        preferences.put("profile.default_content_settings.popups", 0);
        preferences.put("download.default_directory", PATH_TO_DOWNLOAD_DIR);
        preferences.put("profile.password_manager_enabled", false);
        preferences.put("profile.password_manager_leak_detection", false);
        chromeOptions.addExtensions(new File(urlBlockerExtension.toAbsolutePath().toString()));
        if (!Hook.executingEnv.toLowerCase().contains("jenkins")) {
            chromeOptions.addExtensions(
                    new File(modHeaderExtension.toAbsolutePath().toString()));
        }
        chromeOptions.setExperimentalOption("prefs", preferences);
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--disable-gpu");
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            // in Linux, we need these config to work
            LOG.info("Working in Linux OS");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-shm-usage");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--disable-setuid-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-using");
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("disable-infobars");
        }
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--lang=en");
        chromeOptions.addArguments("--enable-javascript");
        chromeOptions.addArguments("--disable-search-engine-choice-screen");
        chromeOptions.setExperimentalOption("w3c", true);
        chromeOptions.setCapability("browserName", "chrome");
        chromeOptions.setAcceptInsecureCerts(true);
        chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

        /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
        if (proxy) {
            Proxy proxyBrowser = new Proxy();
            getProxyServer();
            proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
            chromeOptions.setCapability("proxy", proxyBrowser);
        }

        if (incognito) {
            LOG.info("adding incognito to Browser");
            chromeOptions.addArguments("--incognito");
        }
        driver = new ChromeDriver(chromeOptions);
        return driver;
    }

    private static RemoteWebDriver initChromeHeadlessUserAgent(String userAgent, boolean proxy, boolean incognito) {
        RemoteWebDriver driver;
        LoggingPreferences logPrefsCHROME = new LoggingPreferences();
        logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

        ChromeOptions chromeOptions = new ChromeOptions();
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("profile.default_content_setting_values.notifications", 2);
        preferences.put("plugins.plugins_disabled", new String[] {"Chrome PDF Viewer"});
        preferences.put("download.default_directory", PATH_TO_DOWNLOAD_DIR);
        preferences.put("plugins.always_open_pdf_externally", true);
        preferences.put("profile.default_content_settings.popups", 0);
        chromeOptions.setExperimentalOption("prefs", preferences);
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--remote-allow-origins=*");

        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-shm-usage");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-setuid-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-using");
        chromeOptions.addArguments("window-size=1920,1080");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--headless=new");
        chromeOptions.addArguments("--user-agent=" + userAgent);
        chromeOptions.addArguments("--lang=en");
        chromeOptions.addArguments("--enable-javascript");
        chromeOptions.addExtensions(
                new File(urlBlockerExtension.toAbsolutePath().toString()));
        if (!Hook.executingEnv.toLowerCase().contains("jenkins")) {
            chromeOptions.addExtensions(
                    new File(modHeaderExtension.toAbsolutePath().toString()));
        }
        chromeOptions.setExperimentalOption("w3c", true);
        chromeOptions.setCapability("browserName", "chrome");
        chromeOptions.setAcceptInsecureCerts(true);
        chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

        /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
        if (proxy) {
            Proxy proxyBrowser = new Proxy();
            getProxyServer();
            proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
            chromeOptions.setCapability("proxy", proxyBrowser);
        }

        if (incognito) {
            LOG.info("adding incognito to Browser");
            chromeOptions.addArguments("--incognito");
        }
        driver = new ChromeDriver(chromeOptions);
        return driver;
    }

    public static RemoteWebDriver initChromeHeadless(boolean incognito, boolean proxy) {
        RemoteWebDriver driver;
        LoggingPreferences logPrefsCHROME = new LoggingPreferences();
        logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

        ChromeOptions chromeOptions = new ChromeOptions();
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("profile.default_content_setting_values.notifications", 2);
        preferences.put("profile.default_content_settings.popups", 0);
        chromeOptions.setExperimentalOption("prefs", preferences);
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--remote-allow-origins=*");

        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-shm-usage");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-setuid-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-using");
        chromeOptions.addArguments("window-size=1920,1080");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--headless=new");
        chromeOptions.addExtensions(
                new File(urlBlockerExtension.toAbsolutePath().toString()));
        if (!Hook.executingEnv.toLowerCase().contains("jenkins")) {
            chromeOptions.addExtensions(
                    new File(modHeaderExtension.toAbsolutePath().toString()));
        }
        chromeOptions.addArguments("--lang=en");
        chromeOptions.addArguments("--enable-javascript");
        chromeOptions.addArguments("--disable-search-engine-choice-screen");
        chromeOptions.setExperimentalOption("w3c", true);
        chromeOptions.setCapability("browserName", "chrome");
        chromeOptions.setAcceptInsecureCerts(true);
        chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

        /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
        if (proxy) {
            Proxy proxyBrowser = new Proxy();
            getProxyServer();
            proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
            chromeOptions.setCapability("proxy", proxyBrowser);
        }

        if (incognito) {
            LOG.info("adding incognito to Browser");
            chromeOptions.addArguments("--incognito");
        }
        ChromeDriverService driverService = ChromeDriverService.createDefaultService();
        driver = new ChromeDriver(driverService, chromeOptions);
        // Setup to enable download file in headless
        enableDownloadFileHeadless(driverService.getUrl().toString(), driver);
        return driver;
    }

    public static synchronized RemoteWebDriver initDriver(String browser, boolean incognito, boolean proxy) {
        RemoteWebDriver driver = null;
        switch (browser) {
            case "chrome":
                driver = initChrome(incognito, proxy);
                break;
            case "chromeHeadless":
                driver = initChromeHeadless(incognito, proxy);
                setUpUserAgentOfDriver(driver);
                driver = initChromeHeadlessUserAgent(TestDataLoader.getTestData("@TD:user-agent"), proxy, incognito);
                break;
            default:
                LOG.error("Browser {} is not supported by test automation framework!", browser);
                System.exit(0);
        }
        setCurrentDriver(driver);
        LOG.info(
                "The current thread id '{}' has the following active browsers: {}",
                Thread.currentThread().getName(),
                threadLocalActiveBrowsers.get());
        return driver;
    }

    private static void setUpUserAgentOfDriver(RemoteWebDriver driver) {
        // Get user-agent in chrome headless
        String userAgent = driver.executeScript("return navigator.userAgent;").toString();
        LOG.info("Driver \"{}\" has user-agent \"{}\"", driver, userAgent);
        // Convert to user-agent chrome and set in test data
        TestDataLoader.setTestData("user-agent", userAgent.replace("HeadlessChrome", "Chrome"));
        LOG.info("Unused driver \"{}\" will be closed", driver);
        driver.quit();
        LOG.info("Close unused driver successfully!");
    }

    public static void setCurrentDriver(RemoteWebDriver remoteWebDriver) {
        threadLocalActiveBrowsers.get().put("current", remoteWebDriver);
    }

    public static RemoteWebDriver getBrowser(String browserName) {
        return threadLocalActiveBrowsers.get().get(browserName);
    }

    public static void getProxyServer() {
        proxyHost = TestDataLoader.getTestData("@TD:proxyHost");
        proxyPort = TestDataLoader.getTestData("@TD:proxyPort");
    }

    public static RemoteWebDriver initDriverWithUserAgent(String browserName, String userAgent) {
        RemoteWebDriver driver;
        switch (browserName) {
            case "chromeHeadless":
                driver = initChromeHeadlessUserAgent(userAgent, false, false);
                break;
            case "chrome":
                driver = initChrome(false, false);
                break;
            default:
                throw new CucumberException(
                        "Browser name should be one of the following values: chromeHeadless, chromeGCP, chrome, edgeGCP, edge, firefoxGCP, firefox");
        }
        setCurrentDriver(driver);
        LOG.info(
                "The current thread id '{}' has the following active browsers: {}",
                Thread.currentThread().getName(),
                threadLocalActiveBrowsers.get());
        return driver;
    }

    // Setup to enable download file in headless
    private static void enableDownloadFileHeadless(String driverServiceUrl, RemoteWebDriver driver) {
        Map<String, Object> commandParams = new HashMap<>();
        commandParams.put("cmd", "Page.setDownloadBehavior");
        Map<String, String> params = new HashMap<>();
        params.put("behavior", "allow");
        params.put("downloadPath", PATH_TO_DOWNLOAD_DIR);
        commandParams.put("params", params);
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            String command = objectMapper.writeValueAsString(commandParams);
            String url = driverServiceUrl + "/session/" + driver.getSessionId() + "/chromium/send_command";
            LOG.info("Driver service url is: {}", driverServiceUrl);
            LOG.info("Driver session id is: {}", driver.getSessionId());
            HttpPost request = new HttpPost(url);
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity(command));
            httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToBrowser(String browserName) {
        threadLocalActiveBrowsers.get().put("current", getBrowser(browserName));
    }

    public static void closeBrowser(String browserName) {
        getBrowser(browserName).quit();
        threadLocalActiveBrowsers.get().remove(browserName);
    }
    public static void closeDriver() {
        LOG.info(
                "{} remaining open Browsers: {}",
                BasePage.CURRENT_THREAD_SCENARIO.get(),
                threadLocalActiveBrowsers.get().keySet());
        threadLocalActiveBrowsers.get().keySet().forEach(driverAlias -> {
            threadLocalActiveBrowsers.get().get(driverAlias).quit();
            LOG.info("{} Driver: {} closed as expected :)", BasePage.CURRENT_THREAD_SCENARIO.get(), driverAlias);
        });
        driver = null;
    }
}
