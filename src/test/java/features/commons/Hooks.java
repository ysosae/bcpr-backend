package features.commons;


import com.google.gson.JsonObject;
import config.AbstractAPI;
import config.RestAssuredPropertiesConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import config.RestAssuredExtension;


import static allure.AllureLogger.attachScreenShot;
import static enums.FilesPath.COGNITO_USERS_FILE_LOCATION;
import static utils.CognitoUserHandler.retrieveUserListFromCognitoMatches;
import static utils.CognitoUserHandler.saveInJsonFile;

public class Hooks extends AbstractAPI {
  private static final Logger log = Logger.getLogger(Hooks.class);
  public static Scenario scenario = null;
  static RestAssuredExtension restAssuredExtension = new RestAssuredExtension();


  @BeforeAll
  public static void getCognitoUsers(){
    if(!StringUtils.containsIgnoreCase(RestAssuredPropertiesConfig.getMiddlewareRunDirect(), "true")){
      if(rest.getCognitoUsers()){
        JsonObject cognitoUsersResult = retrieveUserListFromCognitoMatches();
        saveInJsonFile(COGNITO_USERS_FILE_LOCATION.getText(), cognitoUsersResult);
      }
    }
  }

  /** @Before public void before() { this.scenario = scenario; } */
  @Before
  public void initialization(Scenario scenario){
    log.info("***********************************************************************************************************");
    log.info("[ Configuration ] - Initializing  configuration");
    log.info("***********************************************************************************************************");
    Hooks.scenario = scenario;
    restAssuredExtension.dynamo(true);
    restAssuredExtension.dynamoDBClient();
    log.info("***********************************************************************************************************");
    log.info("[ Scenario ] - " + scenario.getName());
    log.info("***********************************************************************************************************");
  }

  @After
  public void afterMethod(Scenario scenario){
    if (scenario.isFailed()) {
      String scenarioName = scenario.getName();
      log.info("Scenario failed: " + scenarioName);
      attachScreenShot("Screenshot was added");
    }

    log.info("***********************************************************************************************************");
    log.info("[ Configuration ] - Close Initializing  configuration");
    log.info("***********************************************************************************************************");
  }
}
