package features.selenium.config;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static org.openqa.selenium.Proxy.ProxyType.MANUAL;

@Slf4j
public class DriverFactory {


  private WebDriver webdriver;
  private DriverType selectedDriverType;

  private final DriverType defaultDriverType = DriverType.CHROME;
  private final String browser = System.getProperty("browser", defaultDriverType.toString()).toUpperCase();
  private final String operatingSystem = System.getProperty("os.name").toUpperCase();
  private final String systemArchitecture = System.getProperty("os.arch");
  private final boolean useRemoteWebDriver = Boolean.getBoolean("remoteDriver");
  private final boolean proxyEnabled = Boolean.getBoolean("proxyEnabled");
  private final String proxyHostname = System.getProperty("proxyHost");
  private final Integer proxyPort = Integer.getInteger("proxyPort");
  private final String proxyDetails = String.format("%s:%d", proxyHostname, proxyPort);

  public WebDriver getDriver() throws Exception {
    if (null == webdriver) {
      Proxy proxy = null;
      if (proxyEnabled) {
        proxy = new Proxy();
        proxy.setProxyType(MANUAL);
        proxy.setHttpProxy(proxyDetails);
        proxy.setSslProxy(proxyDetails);
      }
      determineEffectiveDriverType();
      DesiredCapabilities desiredCapabilities = selectedDriverType.getDesiredCapabilities(proxy);
      instantiateWebDriver(desiredCapabilities);
    }

    return webdriver;
  }

  public void quitDriver() {
    if (null != webdriver) {
      webdriver.quit();
    }
  }

  private void determineEffectiveDriverType() {
    DriverType driverType = defaultDriverType;
    try {
      driverType = DriverType.valueOf(browser);
    } catch (IllegalArgumentException ignored) {
      log.error("Unknown driver specified, defaulting to '" + driverType + "'...");
    } catch (NullPointerException ignored) {
      log.error("No driver specified, defaulting to '" + driverType + "'...");
    }
    selectedDriverType = driverType;
  }

  private void instantiateWebDriver(DesiredCapabilities desiredCapabilities) throws MalformedURLException {
    log.info(" ");
    log.info("Current Operating System: " + operatingSystem);
    log.info("Current Architecture: " + systemArchitecture);
    log.info("Current Browser Selection: " + selectedDriverType);
    log.info(" ");
    log.info("Remote flag is set to " + useRemoteWebDriver);

    if (useRemoteWebDriver) {
      URL seleniumGridURL = new URL(System.getProperty("gridURL"));
      String desiredBrowserVersion = System.getProperty("desiredBrowserVersion");
      String desiredPlatform = System.getProperty("desiredPlatform");

      if (null != desiredPlatform && !desiredPlatform.isEmpty()) {
        desiredCapabilities.setPlatform(Platform.valueOf(desiredPlatform.toUpperCase()));
      }

      if (null != desiredBrowserVersion && !desiredBrowserVersion.isEmpty()) {
        desiredCapabilities.setVersion(desiredBrowserVersion);
      }

      webdriver = new RemoteWebDriver(seleniumGridURL, desiredCapabilities);
    } else {
      webdriver = selectedDriverType.getWebDriverObject(desiredCapabilities);
    }
  }
}
