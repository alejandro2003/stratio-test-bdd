package com.stratio.specs;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Collections;
import org.elasticsearch.ElasticsearchException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.internal.ApacheHttpClient;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.stratio.exceptions.DBException;
import com.stratio.tests.utils.ThreadProperty;
import com.stratio.tests.utils.RemoteSSHConnection;
import com.thoughtworks.selenium.SeleniumException;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 */
public class HookGSpec extends BaseGSpec {

    public static final int ORDER_10 = 10;
    public static final int ORDER_20 = 20;
    public static final int PAGE_LOAD_TIMEOUT = 120;
    public static final int IMPLICITLY_WAIT = 10;
    public static final int SCRIPT_TIMEOUT = 30;

    /**
     * Default constructor.
     *
     * @param spec
     */
    public HookGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Clean the exception list.
     */
    @Before(order = 0)
    public void globalSetup() {
        commonspec.getLogger().info("Clearing exception list");
        commonspec.getExceptions().clear();
    }

    /**
     * Connect to Cassandra.
     */
    @Before(order = ORDER_10, value = "@C*")
    public void cassandraSetup() {
        commonspec.getLogger().info("Setting up C* client");
        commonspec.getCassandraClient().connect();
    }

    /**
     * Connect to MongoDB.
     */
    @Before(order = ORDER_10, value = "@MongoDB")
    public void mongoSetup() {
        commonspec.getLogger().info("Setting up MongoDB client");
        try {
            commonspec.getMongoDBClient().connect();
        } catch (DBException e) {
            fail(e.toString());
        }
    }

    /**
     * Connect to ElasticSearch.
     */
    @Before(order = ORDER_10, value = "@elasticsearch")
    public void elasticsearchSetup() {
        commonspec.getLogger().info("Setting up elasticsearch client");
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        commonspec.getElasticSearchClient().setSettings(settings_map);
        try {
            commonspec.getElasticSearchClient().connect();
        }catch(UnknownHostException e){
            fail(e.toString());
        }
    }

    /**
     * Connect to Aerospike.
     */
    @Before(order = ORDER_10, value = "@Aerospike")
    public void aerospikeSetup() {
        commonspec.getLogger().info("Setting up Aerospike client");
        commonspec.getAerospikeClient().connect();
    }

    /**
     * Connect to selenium.
     *
     * @throws MalformedURLException
     */
    @Before(order = ORDER_10, value = "@web")
    public void seleniumSetup() throws MalformedURLException {
        String grid = System.getProperty("SELENIUM_GRID");
        if (grid == null) {
            fail("Selenium grid not available");
        }

	String b = ThreadProperty.get("browser");
        if ("".equals(b)) {
            fail("Non available browsers");
        }

        String browser = b.split("_")[0];
        String version = b.split("_")[1];
        commonspec.setBrowserName(browser);
        commonspec.getLogger().info("Setting up selenium for {}", browser);

        DesiredCapabilities capabilities = null;

        switch (browser.toLowerCase()){
        case "chrome":
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--no-sandbox");
            capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
            break;
        case "firefox":
            capabilities = DesiredCapabilities.firefox();
            break;
        case "phantomjs":
            capabilities = DesiredCapabilities.phantomjs();
            break;
        case "iphone":
            capabilities = DesiredCapabilities.iphone();
            capabilities.setCapability("platformName", "iOS");
            capabilities.setCapability("platformVersion", "8.1");
            capabilities.setCapability("deviceName", "iPhone Simulator");
            break;
        case "safari":
            capabilities = DesiredCapabilities.safari();
            capabilities.setCapability("platformName", "iOS");
            capabilities.setCapability("platformVersion", "8.1");
            capabilities.setCapability("deviceName", "iPhone Simulator");
            break;
        case "android":
            capabilities = DesiredCapabilities.android();
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("platformVersion", "6.0");
            capabilities.setCapability("deviceName", "Android Emulator");
            capabilities.setCapability("app", "Browser");
            break;
        default:
            commonspec.getLogger().error("Unknown browser: " + browser);
            throw new SeleniumException("Unknown browser: " + browser);
        }

        capabilities.setVersion(version);

        grid = "http://" + grid + "/wd/hub";
        HttpClient.Factory factory = new ApacheHttpClient.Factory(new HttpClientFactory(60000, 60000));
        HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), new URL(grid), factory);
        commonspec.setDriver(new RemoteWebDriver(executor, capabilities));
        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);

        commonspec.getDriver().manage().deleteAllCookies();
        if (capabilities.getCapability("deviceName") == null) {
            commonspec.getDriver().manage().window().setSize(new Dimension(1440, 900));
        }
        // commonspec.getDriver().manage().window().maximize();

    }

    /**
     * Close selenium web driver.
     */
    @After(order = ORDER_20, value = "@web")
    public void seleniumTeardown() {
        if (commonspec.getDriver() != null) {
            commonspec.getLogger().info("Shutdown Selenium client");
            commonspec.getDriver().close();
            commonspec.getDriver().quit();
        }
    }

    /**
     * Close cassandra connection.
     */
    @After(order = ORDER_20, value = "@C*")
    public void cassandraTeardown() {
        commonspec.getLogger().info("Shutdown  C* client");
        try {
            commonspec.getCassandraClient().disconnect();
        } catch (DBException e) {
            fail(e.toString());
        }
    }

    /**
     * Close MongoDB Connection.
     */
    @After(order = ORDER_20, value = "@MongoDB")
    public void mongoTeardown() {
        commonspec.getLogger().info("Shutdown MongoDB client");
        commonspec.getMongoDBClient().disconnect();
    }

    /**
     * Close ElasticSearch connection.
     */
    @After(order = ORDER_20, value = "@elasticsearch")
    public void elasticsearchTeardown() {
        commonspec.getLogger().info("Shutdown elasticsearch client");
        try {
            commonspec.getElasticSearchClient().getClient().close();
        } catch (ElasticsearchException e) {
            fail(e.toString());
        }
    }

    /**
     * Close aerospike connection.
     */
    @After(order = ORDER_20, value = "@Aerospike")
    public void aerospikeTeardown() {
        commonspec.getLogger().info("Shutdown Aerospike client");
        try {
            commonspec.getAerospikeClient().disconnect();
        } catch (DBException e) {
            fail(e.toString());
        }
    }

    /**
     * Close logger.
     */
    @After(order = 0)
    public void teardown() {
        commonspec.getLogger().info("Ended running hooks");
    }

    @Before(order = 10, value = "@rest")
    public void restClientSetup() throws Exception {
        commonspec.getLogger().info("Starting a REST client");

        commonspec.setClient(new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(false)
                .build()));
    }

    @After(order = 10, value = "@rest")
    public void restClientTeardown() throws IOException {
        commonspec.getLogger().info("Shutting down REST client");
        commonspec.getClient().close();
    }

    @After(order = 10)
    public void remoteSSHConnectionTeardown() throws Exception {
        commonspec.getLogger().info("Closing SSH remote connection");
        if (commonspec.getRemoteSSHConnection() != null) {
            commonspec.getRemoteSSHConnection().getSession().disconnect();
        }
    }
}
