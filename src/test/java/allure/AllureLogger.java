package allure;


import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;
import static enums.FilesPath.GLOBAL_DATA_FILE_LOCATION;

import com.google.common.collect.ImmutableMap;
import config.AbstractAPI;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class AllureLogger {
  private static final Logger log = Logger.getLogger(AllureLogger.class);
  private static final Properties prop = new Properties();
  private static final String LOG_HEADER = "Mi Tarjeta Coop | ";
  private static final String NO_CONTENT = "Nothing to report!";
  private static final String BASE_PATH = System.getProperty("user.dir") + "/allure-results/";
  private static String ENV = null;

  static {
    try {
      ENV = getEnv();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  public static String getEnv() throws IOException {
    InputStream input;
    input = new FileInputStream(GLOBAL_DATA_FILE_LOCATION.getText());
    prop.load(input);
    return org.codehaus.plexus.util.StringUtils.isNotEmpty(prop.getProperty("env"))
      ? prop.getProperty("env")
      : null;
  }

  /**
   * Log a no context message to the console. This is mainly used in the
   * TestNG configuration methods and listeners.
   */
  public void printLog() {
    printWrapper(getPrettifyMessage(NO_CONTENT));
  }



  /**
   * Log a message to the console and to the Allure report.
   *
   * @param message to send
   */
  public void printLogToReport(String message) {
    printToReport(message);
    printWrapper(getPrettifyMessage(message));
  }

  private String getPrettifyMessage(String rawMessage) {
    StackTraceElement trace = Thread.currentThread().getStackTrace()[3];
    String callingMethod =
      StringUtils.substringAfterLast(trace.getClassName(), ".") + "." + trace.getMethodName()
        + "()";
    return callingMethod + " | " + rawMessage;
  }

  @Step("{0}")
  private void printToReport(String prettyMessage) {
    // Shell method for annotation reader
  }

  private void printWrapper(String message) {
    log.info(LOG_HEADER + message);
  }

  public void deleteOldAllureReports() {
    File projectFile = new File("target/allure-results");
    FileUtils.deleteQuietly(projectFile);
  }

  /**
   * Set environment variables into allure report
   */
  public void setEnvironment() {
    String testExecutionId = "BCPRXRP-XXX";
    allureEnvironmentWriter(ImmutableMap.<String, String>builder()
      .put("Environment", ENV)
      .put("Test Execution ID", testExecutionId)
      .build(), BASE_PATH);
  }

  /**
   * @param name String
   */
  public static void attachScreenShot(String name) {
    log.info("Taking screenshot");
    try {
      Allure.addAttachment(name, AbstractAPI.response.getBody().asInputStream());
    } catch (Exception e) {
      log.error("Error taking screenshot : {}" + e.getMessage());
    }
  }

  /**
   * @param name String
   * @param value String
   */
  public static void attachScreenShot(String name, String value) {
    log.info("Taking screenshot");
    try {
      Allure.addAttachment(name, value);
    } catch (Exception e) {
      log.error("Error taking screenshot : {} " + e.getMessage());
    }

  }
}
