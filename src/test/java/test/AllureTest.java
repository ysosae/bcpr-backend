package test;

import allure.AllureLogger;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class AllureTest {
  private static final Logger log = Logger.getLogger(AllureTest.class);
  protected AllureLogger report = new AllureLogger();

  @Test
  public void onExecutionStart() {
    log.info("Cleaning Allure Report.");
    report.deleteOldAllureReports();
  }

  @Test
  public void onStart() {
    log.info("Setting Allure Environment");
    report.setEnvironment();
    report.printLog();
    report.printLogToReport("Send Logs Test");
  }
}
