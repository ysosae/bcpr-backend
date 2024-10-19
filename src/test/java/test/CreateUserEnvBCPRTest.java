package test;

import static config.RestAssuredPropertiesConfig.awsResources;
import static storage.ScenarioContext.getScenarioContextVariables;
import static storage.ScenarioContext.saveInScenarioContext;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.JsonObject;
import config.AbstractAPI;
import config.RestAssuredExtension;
import config.RestAssuredPropertiesConfig;
import features.commons.CommonsStepDefs;
import features.graphQL.StepDefinitions;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateUserEnvBCPRTest extends AbstractAPI {

  public static ResponseOptions<Response> response = null;
  private static final Logger log = Logger.getLogger(CreateUserEnvBCPRTest.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static StepDefinitions steps = new StepDefinitions();
  public static CommonsStepDefs commonsStepDefs = new CommonsStepDefs();
  public static String username = null;

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUsersBase")
  public void createUsersBase() {
    createUserCognito("Yalithza2023");
    createUserCognito("Facundo2023");
    createUserCognito("Yuliet2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserFacundo")
  public void createUserFacundo() {
    createUserCognito("Facundo2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserCandido")
  public void createUserCandido() {
    createUserCognito("Candido2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserYuliet")
  public void createUserYuliet() {
    createUserCognito("Yuliet2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserYalithza")
  public void createUserYalithza() {
    createUserCognito("Yalithza2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserEdgardo")
  public void createUserEdgardo() {
    createUserCognito("Edgardo2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserAndres")
  public void createUserAndres() {
    createUserCognito("Andres2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserPatria")
  public void createUserPatria() {
    createUserCognito("Patria2023");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserMarisol")
  public void createUserMarisol() {
    createUserCognito("Marisol1312");
  }

  @Test(
    groups = {"CreateUserEnvBCPRTest"},
    testName = "createUserAutomation2023")
  public void createUserAutomation2023() {
    createUserCognito("Automation2023");
  }

  public void createUserCognito(String username) {
    if (StringUtils.equalsIgnoreCase(username, "Facundo2023")) {
      createUser("../Users/getEnrollmentValidationFacundo.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Yuliet2023")) {

      createUser("../Users/getEnrollmentValidationYuliet.graphql", username);


    } else if (StringUtils.equalsIgnoreCase(username, "Yalithza2023") ||
      StringUtils.equalsIgnoreCase(username, "Yalithza2023")) {

      createUser("../Users/getEnrollmentValidationYalitza.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Marisol1312")) {

      createUser("../Users/getEnrollmentValidationMarisol.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Edgardo2023")) {

      createUser("../Users/getEnrollmentValidationEdgardo.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Automation2023")) {

      createUser("../Users/getEnrollmentValidationLucy.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Andres2023")) {

      createUser("../Users/getEnrollmentValidationAndres.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Patria2023")) {

      createUser("../Users/getEnrollmentValidationPatria.graphql", username);

    } else if (StringUtils.equalsIgnoreCase(username, "Candido2023")) {

      createUser("../Users/getEnrollmentValidationCandido.graphql", username);

    }

  }

  public void createUser(String bodyEnrollment, String username) {
    String env = awsResources.getPrefix();
    if(!StringUtils.equalsIgnoreCase(env, "BCPR-CRT")){
      deleteCognitoUser(username);
    }
    isUserCreation = true;
    response = postMethodGraphQL(bodyEnrollment);
    String key = "enrollmentId";
    steps.savePathDataAsContextVariable(key, "data.enrollmentValidation.enrollmentId");
    if (scenarioData != null) {
      if (scenarioData.has(key)) {
        String enrollmentId = getScenarioContextVariables(key);
        boolean isError = response.getBody().prettyPrint().contains("error");
        if (!isError) {
          filterCognito(enrollmentId, "DATA_VALIDATED");
        } else {
          String ssn= getKeyBodyEnrollment(bodyEnrollment,"socialSecurityNumber");
          log.info(String.format("The SSN %s already exists into env %s%n", ssn,
            RestAssuredPropertiesConfig.getEnvironment()));
          log.error("The data is wrong or SSN already exists into cognito");
        }
      } else {
        System.out.printf("The value stored in the key %s is %s%n", key, null);
      }
      try {
        commonsStepDefs.shortWaits();
      } catch (InterruptedException e) {
       log.error(e.getMessage());
      }
      response = postMethodGraphQL("graphQL/getCodeValidation.graphql");
      saveInScenarioContext("username", username);
      String newPass = setPasswordBase();
      saveInScenarioContext("password", newPass);
      response = postMethodGraphQL("graphQL/getEnrollUser.graphql");
      String pathValue = "";
      String PATH = "data.enrollUser.status";
      try {
        pathValue = response.getBody().jsonPath().getString(PATH);
      } catch (Exception e) {
        log.error(e.toString());
      }
      Assert.assertTrue(
        StringUtils.containsIgnoreCase(pathValue, "USER_CREATED"),
        String.format(
          "in path %s, expected value was %s, but is different",
          PATH, "USER_CREATED"));

      saveInScenarioContext("USERNAME", username);
      saveInScenarioContext("PASSWORD", newPass);
      isUserCreation = false;
      response = postMethodGraphQL("graphQL/login.graphql");
    } else {
      log.error("The variable %s is null.");
    }
  }

   public String getKeyBodyEnrollment(String bodyEnrollment, String key){
     String[] pairs;
     String stringValue=null;
     try {
       pairs = setBodyStringFormat(bodyEnrollment, "userArgs:");
       buildMapExtractKeysValues(pairs);
       JsonObject jsonObjectEnrollment = new JsonObject();
       stringValue=jsonObjectEnrollment.get(key).toString();

  } catch (Exception e) {
       log.error("Error parsing the JSON string: " + e.getMessage());
     }
     return stringValue;
  }

  public static String[] setBodyStringFormat(String bodyEnrollment, String argumentsName){
    String valueString;
    String[] pairs;
    valueString =RestAssuredExtension.getBodyFromResource(bodyEnrollment);
    String bodyFormat=StringUtils.substringBefore(valueString,")");
    bodyFormat= StringUtils.substringAfter(bodyFormat,argumentsName);
    pairs= StringUtils.substringsBetween(bodyFormat,"{", "}");
    if(StringUtils.containsIgnoreCase(bodyFormat,"socialSecurityNumber")){
      pairs = pairs[0].split("(\\w+):(\\w+)");
    }
    return pairs;
  }

  public static void buildMapExtractKeysValues(String[] pairs){
    Map<String, String> map = new HashMap<>();
    ArrayList<String> formatKeyString= new ArrayList<>();
    ArrayList<String> formatValueString= new ArrayList<>();
    for (String pair : pairs) {
      String[] formatPairs = pair.split("\\s+");
      for (String format: formatPairs) {
        if(StringUtils.containsIgnoreCase(format,":")){
          formatKeyString.add(addDoubleQuotes(format));
        }if(StringUtils.containsIgnoreCase(format,"\"")){
          formatValueString.add(format);
        }
      }
    }
    for (int i=0;i<formatKeyString.size();i++){
      String KEY=formatKeyString.get(i).replaceAll("\"","").replaceAll(":","");
      String VALUE=formatValueString.get(i).replaceAll("\"","");
      map.put(KEY,VALUE);
    }
  }


  public static String addDoubleQuotes(String input) {
    return "\"" + input + "\"";
  }

  public void filterCognito(String enrollmentId, String status) {
    Map<String, AttributeValue> getData;
    rest.putExpressionAttributeValues("String", ":enrollmentId", enrollmentId);
    rest.putExpressionAttributeValues("String", ":status_", status);

    RestAssuredExtension.putExpressionAttributeNames("#status", "status");

    String tabla = String.format("%s-EnrollmentData",
      RestAssuredPropertiesConfig.getEnvironment().replaceFirst("EVT", "BCPR"));
    ScanResult result =
      rest.ScanAction(tabla, "enrollmentId = :enrollmentId and #status = :status_");

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        getData = item.get("data").getM();
        log.info("The data filter %s%n" + getData);
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
      saveInScenarioContext("phoneCode", item.get("phoneCode").getN());
    }
  }
}
