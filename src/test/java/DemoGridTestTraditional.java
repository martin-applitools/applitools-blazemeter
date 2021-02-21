import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class DemoGridTestTraditional {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoGridTestTraditional.class);

    private final static String API_KEY = System.getenv("API_KEY");
    private final static String API_SECRET = System.getenv("API_SECRET");
    private final static String BASE = "a.blazemeter.com";
    private final static String baselineURL = "http://applitoolsjenkins.eastus.cloudapp.azure.com:5000/demo.html";
    private final static String currentBuildURL = "http://applitoolsjenkins.eastus.cloudapp.azure.com:5000/demo.html?version=2";
    private final static String curl = String.format("https://%s/api/v4/grid/wd/hub", BASE);
    public static RemoteWebDriver driver;

    @Rule
    public final TestName bzmTestCaseReporter = new TestName() {

        @Override
        protected void starting(Description description) {
            Map<String, String> map = new HashMap<>();
            map.put("testCaseName", description.getMethodName());
            map.put("testSuiteName", description.getClassName());
            driver.executeAsyncScript("/* FLOW_MARKER test-case-start */", map);
        }

        @Override
        protected void succeeded(Description description) {
            if (driver != null) {
                Map<String, String> map = new HashMap<>();
                map.put("status", "success");
                map.put("message", "");
                driver.executeAsyncScript("/* FLOW_MARKER test-case-stop */", map);
            }
        }

        @Override
        protected void failed(Throwable e, Description description) {
            Map<String, String> map = new HashMap<>();
            if (e instanceof AssertionError) {
                map.put("status", "broken");
            } else {
                map.put("status", "failed");
            }
            map.put("message", e.getMessage());
            driver.executeAsyncScript("/* FLOW_MARKER test-case-stop */", map);
        }
    };

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        URL url = new URL(curl);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("blazemeter.apiKey", API_KEY);
        capabilities.setCapability("blazemeter.apiSecret", API_SECRET);
        capabilities.setCapability("blazemeer.reportName", "Functional Test");
        capabilities.setCapability("blazemeter.sessionName", "Chrome browser test");
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("browserVersion", "88");
        driver = new RemoteWebDriver(url, capabilities);
        String reportURL = String.format("https://%s/api/v4/grid/sessions/%s/redirect/to/report", BASE,
                driver.getSessionId());
        System.out.println("Report url: " + reportURL);
        //openInBrowser(reportURL);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public static void openInBrowser(String string) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI(string));
            } catch (Exception ex) {
                LOGGER.warn("Failed to open in browser", ex);
            }
        }
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testLoginNoCredentialsClassicValidation() throws InterruptedException {
       //Navigate to Site 
        driver.get(currentBuildURL);
        Thread.sleep(2000);
        //Verify no error messages are displayed initially
        assertFalse(driver.findElementById("alert").isDisplayed());
        //Assert username label is correct
        assertEquals("Username", driver.findElement(By.xpath("//*[@id='main-form']/div[1]/label")).getText());
        //Assert password label is correct
        assertEquals("Password", driver.findElement(By.xpath("//label[@for='password']")).getText());
        //Assert if SignIn buttons label is "Sign In"
        assertEquals("Sign In", driver.findElement(By.id("log-in")).getText());
        //Assert Remember Me checkbox exists
		assertEquals(1, driver.findElementsByXPath("//label[text()='Remember Me']/input[@type='checkbox']").size());
        //Assert if Twitter button exists
		assertEquals(1, driver.findElements(By.xpath("//img[contains(@src, 'twitter.png')]")).size());
        //Assert if Facebook button exists
        assertEquals(1, driver.findElements(By.xpath("//img[@id='facebook']")).size());
        //Find and Click the Login Button
        driver.findElement(By.id("log-in")).click();
        //Assert the error text
        assertEquals("Please enter username and password", driver.findElement(By.id("alert")).getText().trim());
    }
}
