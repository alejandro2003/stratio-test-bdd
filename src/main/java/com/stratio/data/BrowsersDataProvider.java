package com.stratio.data;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;

import com.google.common.collect.Lists;


public final class BrowsersDataProvider {
    public static final int DEFAULT_TIMEOUT = 20000;
    public static final int DEFAULT_LESS_LENGTH = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsersDataProvider.class);

    private BrowsersDataProvider() {
    }

    /**
     * Get the browsers available in a selenium grid.
     * 
     * @param context
     * @param testConstructor
     * @return an iterator
     * @throws Exception
     */
    @DataProvider(parallel = true)
    public static Iterator<String[]> availableBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        List<String> browsers = gridBrowsers();
        List<String[]> lData = Lists.newArrayList();

        for (String s : browsers) {
            lData.add(new String[] { s });
        }

        if (lData.size() == 0) {
            lData.add(new String[] { "" });
        }

        return lData.iterator();
    }

    @DataProvider(parallel = true)
    public static Iterator<String[]> availableUniqueBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {
	
        List<String> browsers = gridBrowsers();

        HashSet<String> hs = new HashSet<String>();
        hs.addAll(browsers);
        browsers.clear();

        browsers.addAll(hs);

        List<String[]> lData = Lists.newArrayList();

        for (String s : browsers) {
            lData.add(new String[] { s });
        }

        if (lData.size() == 0) {
            lData.add(new String[] { "" });
        }

        return lData.iterator();
    }

    private static List<String> gridBrowsers() {
	ArrayList<String> response = new ArrayList<String>();

	String grid = System.getProperty("SELENIUM_GRID");

	if (grid != null) {        
	    grid = "http://" + grid + "/grid/console";
	    Document doc;
	    try {
		doc = Jsoup.connect(grid).timeout(DEFAULT_TIMEOUT).get();
	    } catch (IOException e) {
		LOGGER.debug("Exception on connecting to Selenium grid: {}", e.getMessage());
		return response;
	    }

	    Elements slaves = (Elements) doc.select("div.proxy");

	    for (Element slave : slaves) {
		String slaveStatus = slave.select("p.proxyname").first().text();
		if (!slaveStatus.contains("Connection") && !slaveStatus.contains("Conexión")) {
		    Integer iBusy = 0;
		    Elements browserList = slave.select("div.content_detail").select("*[title]");
		    Elements busyBrowserList = slave.select("div.content_detail").select("p > .busy");
		    for (Element browserDetails : browserList) {
			if (browserDetails.attr("title").startsWith("{")) {
			    Pattern pat = Pattern.compile("browserName=(.*?),.*?(version=(.*?))?}");
			    Matcher m = pat.matcher(browserDetails.attr("title"));
			    while (m.find()) {
				response.add(m.group(1) + "_" + m.group(3));
			    }
			} else {
			    String version = busyBrowserList.get(iBusy).parent().text();
			    String browser = busyBrowserList.get(iBusy).text();
			    version = version.substring(2);
			    version = version.replace(browser, "");
			    String browserSrc = busyBrowserList.get(iBusy).select("img").attr("src");
			    if (!browserSrc.equals("")) {
				browser = browserSrc.substring(browserSrc.lastIndexOf('/') + 1, browserSrc.length()
					- DEFAULT_LESS_LENGTH);
			    }
			    response.add(browser + "_" + version);
			    iBusy++;
			}
		    }
		}
	    }
	}
	// Sort response
	Collections.sort(response);
	return response;
    }
}