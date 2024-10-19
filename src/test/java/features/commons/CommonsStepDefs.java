package features.commons;

import static config.AlarmAWS.buildAlarmNameByResource;
import static config.AlarmAWS.getQueueType;
import static config.AlarmAWS.getStatusAlarm;
import static config.AlarmAWS.isAlarmApproximateNumberOfMessagesVisible;
import static config.DynamoDbAWS.deleteItemDynamoDB;
import static config.DynamoDbAWS.findValueByQueryIntoPayment;
import static config.DynamoDbAWS.getAttributeValueLastPasswordResultDynamoDb;
import static config.DynamoDbAWS.getItemValue;
import static config.DynamoDbAWS.handleDefaultCase;
import static config.DynamoDbAWS.handleLastPasswordTable;
import static config.DynamoDbAWS.handlePaymentTable;
import static config.DynamoDbAWS.handleWalletTable;
import static config.DynamoDbAWS.performPagedErrorPaymentQuery;
import static config.DynamoDbAWS.performPagedPaymentQuery;
import static config.DynamoDbAWS.performPagedWalletQuery;
import static config.DynamoDbAWS.removeExistingAllLastPasswordDynamoDB;
import static config.DynamoDbAWS.removeExistingLastPasswordDynamoDB;
import static config.DynamoDbAWS.removeExistingLoginAttemptsDynamoDB;
import static config.DynamoDbAWS.scannerUserLastPasswordInfo;
import static config.DynamoDbAWS.totalItem;
import static config.QueueAWS.isTrappedMessagesQueue;
import static config.RestAssuredExtension.configProperties;
import static config.RestAssuredExtension.getDefaultUsername;
import static config.RestAssuredExtension.getSessionUser;
import static config.RestAssuredExtension.getSizeResponseGraphql;
import static config.RestAssuredExtension.getSubIdByUsernameIntoLocalData;
import static model.CardActivationValidator.saveWrongLastEightDigits;
import static model.LoginValidator.saveWithEmailAndPassword;
import static model.LoginValidator.saveWrongPassword;
import static model.LoginValidator.saveWrongUsername;
import static storage.ScenarioContext.getScenarioContextVariables;
import static storage.ScenarioContext.saveInScenarioContext;
import static utils.UserDataUtils.getUserByCondition;
import static utils.UserDataUtils.updateUserPermissionsAndReturn;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import config.AbstractAPI;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import enums.ProjectName;
import enums.QueueType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;

public class CommonsStepDefs extends AbstractAPI {
  private static final Logger log = Logger.getLogger(CommonsStepDefs.class);

    @Given("^Set (.*) as main test user$")
    public void setAsMainTestUser(String userType) {
      String user = getUserByCondition(userType);
      if (user == null) {
        log.warn("User with condition '{}' not found, updating roles and permissions. " + userType);
        user = updateUserPermissionsAndReturn(userType);
      }

      RestAssuredExtension.setSessionUser(user);
    }

    @Given("^post a graphQL request using (.*)$")
    public void graphQLRequest(String body) {
        response = postMethodGraphQL(body);
    }

    @Then("^save (.*) search for blockingStatus and save value (.*) as context variable$")
    public void searchKeyFromResponseThatContainsAndSaveContextVariable(
            String path, String saveAs) {
        saveKeyFromResponseThatContainsSaveContextVariable(path, saveAs);
    }

    @Then("^The response code is (.*)$")
    public void theApiCodeResponseIs(String responseNumber) {

        Assert.assertTrue(
                StringUtils.containsIgnoreCase(String.valueOf(response.statusCode()), responseNumber),
                String.format(
                        "The Status Code response is different to expected, obtained: %s, expected: %s",
                        response.statusCode(), responseNumber));
    }

  @Then("^Response code is either (.*) or (.*)$")
  public void theApiCodeResponseIs(String responseNumber, String codeResponse) {
    Assert.assertTrue(
      StringUtils.containsIgnoreCase(String.valueOf(response.statusCode()), responseNumber)
        || StringUtils.containsIgnoreCase(String.valueOf(response.statusCode()), codeResponse),
      String.format(
        "The Status Code response is different to expected, obtained: %s, expected: %s",
        response.statusCode(), responseNumber));
  }

    @Then("^The response is (.*)$")
    public void theApiResponseIs(String responseNumber) {
        Assert.assertTrue(
                StringUtils.containsIgnoreCase(String.valueOf(response.statusCode()), responseNumber),
                String.format(
                        "The Status Code response is different to expected, obtained: %s, expected: %s",
                        response.statusCode(), responseNumber));
    }

    @Then("^Short wait between request$")
    public void shortWaits() throws InterruptedException {
        shortWait(5);
    }

    @Then("^Wait (.*) seconds between request$")
    public void shortWaits(int time) {
        shortWait(time);
    }

    @Then("^response code is different to (.*)$")
    public void theApiCodeResponseIsNot(String responseNumber) {
        Assert.assertNotEquals(responseNumber, String.valueOf(response.statusCode()), String.format(
                "The Status Code response is equal to expected, obtained: %s, expected: %s",
                response.statusCode(), responseNumber));
    }

    @Given("^post graphQL request with body (.*) override table values$")
    public void middlewareRequestOverride(String body, List<List<String>> t_table) {
        response = null;
        overrideData = null;
        if(StringUtils.containsIgnoreCase(body,"login")){
          saveWrongPassword();
          saveWrongUsername();
          saveWithEmailAndPassword();
        }
        if(StringUtils.containsIgnoreCase(body,"CardActivation")){
          saveWrongLastEightDigits();
        }
        overrideData = setOverrideData(t_table);
        response = postMethodGraphQL(body);
        removeExistingLoginAttemptsDynamoDB(t_table.get(0).get(1));
    }

    @Given("^I set the new Password with wrong police requirements with body (.*)$")
    public void setWrongPass(String body, List<List<String>> t_table) {
        response = null;
        overrideData = null;
        saveOldPassAsBackUp();
        saveNewPassWithUsername();
        saveNewPassWithBlacklistedCharacters();
        saveNewPassTooShort();
        invertPasswordPoliceConfig();
        overrideData = setOverrideData(t_table);
        response = postMethodGraphQL(body);
        invertPasswordPoliceConfig();
    }

    @Given("^restore password policy configuration$")
    public void restorePassPolicyConfig() {
        configProperties.setOldPasswordPolice();
        removeOldPassBackUp();
    }

    @Then("^I print out the results of the response$")
    public void iPrintOutTheResultsOfTheResponse() {
        response.getBody().prettyPrint();
    }

  @Then("^I print size results with path (.*) of the response graphql$")
  public void iPrintSizeResultsOfTheResponseGraphql(String path) {
    getSizeResponseGraphql(path);
  }

  @Then("^validate graphql record count (.*) with DynamoDb records (.*)$")
  public void compareRecordGraphqlAndDynamoDb(String path, String subId) {
      if(StringUtils.equalsIgnoreCase(subId, "subId")){
        subId = getSubIdByUsernameIntoLocalData("sub");
        log.info(String.format("The subId %s" , subId));
      }
      int recordGraphql = getSizeResponseGraphql(path);
      totalItem = 0;
      if(StringUtils.containsIgnoreCase(path, "Payments")){
        totalItem = performPagedPaymentQuery(subId).size();
      }
      if(StringUtils.containsIgnoreCase(path, "Wallet")){
        totalItem = performPagedWalletQuery(subId).size();
      }

    Assert.assertEquals(recordGraphql,totalItem, "The record are not equal");
  }

    @Then("^I compare response <Path> show the <Values>$")
    public void iCompareResponsePathShowTheValues(List<List<String>> t_table) {
        compareResponsePathShowTheValues(response, t_table);
    }

    @Given("^Set Expression Attribute (Values|Names) to query$")
    public void setExpressionAttributeValuesToQuery(String action, List<List<String>> t_table) {
        switch (action) {
            case "Values":
                setExpressionAttributeValuesToQuery(t_table);
                break;
            case "Names":
                setExpressionAttributeNamesToQuery(t_table);
                break;
        }
    }

    @Given("^Retrieve data from scan table: (.*) and filter: (.*) save variables bellow$")
    public void retrieveDataFromScanTableAndFilterDataSaveVariablesBellow(
            String table, String filter, List<List<String>> t_table) {
        String prefix= ResourcesAWS.buildPrefixResources(ProjectName.BCPR);
        table = prefix + "-" + table;
        log.info("Table is: " + table);
        retrieveScanAction(table, filter, t_table);
    }

  @Given("^Retrieve data from query table: (.*) using this subId (.*) and status (.*)$")
  public void retrieveDataFromQueryBySubId(String table, String subId, String status){
    String statusFilter="";
    String accountId ="";
    String prefix= ResourcesAWS.buildPrefixResources(ProjectName.BCPR);
    table = prefix + "-" + table;
    log.info("Table is: " + table);
    if(StringUtils.equalsIgnoreCase(subId, "subId")){
      subId = getSubIdByUsernameIntoLocalData("sub");
      log.info(String.format("The subId %s" , subId));
    }
    if(StringUtils.equalsIgnoreCase(status,"CONFIRMED")){
      saveInScenarioContext("status", status);
      if (scenarioResponse.has("accountIdPayments")) {
        accountId = scenarioData.get("accountIdPayments").toString();
      }
      String username = getDefaultUsername();
       statusFilter = findValueByQueryIntoPayment(subId, username, accountId, status);
    }
    if(StringUtils.equalsIgnoreCase(status,"ERROR_PAYMENTS")){
      saveInErrorPaymentsContext("status", status);
      statusFilter = performPagedErrorPaymentQuery(subId);
    }

    retrieveQueryAction(subId, statusFilter);
  }

    @Then("^expected value (.*) into scenario data is equals (.*)$")
    public void valueEqualsScenarioDataAttempts(String value, String scenarioData) {
        if (containsNumber(value)) {
            compareValueWithScenarioData(Integer.parseInt(value), scenarioData);
        } else if (containsString(value)) {
            compareValueStringWithScenarioData(value, scenarioData);
        }
    }

    @Given("^Retrieve data from scan cognito list filter attribute (.*) and user: (.*) expected Enable: (.*)$")
    public void retrieveDataFromScanCognitoFilterAttributeUsername(
            String attribute, String user, String status) {
        retrieveScanCognitoAction(attribute, user, status);
    }

    @Given("^Delete user (.*) from Cognito$")
    public void deleteUserAutomationFromCognito(String user) {
        if (StringUtils.equalsIgnoreCase(user, "username")) {
            user = getSessionUser();
        }
        deleteCognitoUser(user);
    }

    @Given("^Set isUserCreation as (.*)$")
    public void setUserCreation(String value) {
        isUserCreation = Boolean.parseBoolean(value);
    }

    @And("^delete item dynamoDb by table name (.*) and item name (.*)$")
    public void deleteItemDynamoDbByTableNameAndItemName(String nameTable, String itemName) {
      String itemValue = getItemValue(itemName);

      switch (nameTable.toUpperCase()) {
        case "LAST_PASSWORD":
          handleLastPasswordTable(itemName);
          break;
        case "WALLET":
          handleWalletTable(itemName);
          break;
        case "PAYMENT":
          handlePaymentTable(itemName);
          break;
        default:
          handleDefaultCase(nameTable, itemName, itemValue);
      }
    }



  @And("^delete (.*) item dynamoDb by table name (.*) and item name (.*)$")
  public void deleteItemDynamoDbByTableNameAndItemName(String countItem, String nameTable, String itemName) {
    String itemValue=null;
    String lastPassword;
    if(StringUtils.equalsIgnoreCase(countItem, "ALL") && StringUtils.equalsIgnoreCase("LastPassword", nameTable)){
      removeExistingAllLastPasswordDynamoDB(itemName, "id");
    }
    if (StringUtils.equalsIgnoreCase(itemName, "username")) {
      itemValue = getSessionUser();
    }
    if(StringUtils.equalsIgnoreCase("LastPassword", nameTable)){

      lastPassword = getAttributeValueLastPasswordResultDynamoDb(itemName, "last_password");
      log.info(lastPassword);
      removeExistingLastPasswordDynamoDB(itemName, "id");
    }else{
      String prefix= ResourcesAWS.buildPrefixResources(ProjectName.BCPR);
      deleteItemDynamoDB(prefix.concat(String.format("-%s", nameTable)), itemName, itemValue);
    }
  }

    @And("^set status value enable in cognito user$")
    public void setStatusValueEnableInCognitoUser() {
        setStatusEnabledCognito(getSessionUser());
    }

    @And("^add permission (.*) to (.*) in cognito$")
    public void addPermissionAdminToUserInCognito(String permission, String user) {
        if (StringUtils.equalsIgnoreCase(user, "username")) {
            user = getSessionUser();
        }
        addPermissionUserCognito(permission, user);
    }

    @Then("^validate LastPassword entry in DynamoDB for user (.*)$")
    public void validateLastPasswordEntryInDynamoDBForUser(String user) {
        if(StringUtils.equalsIgnoreCase(user,"username")){
            user= getSessionUser();
        }
        String lastPassword = "";
        try {
            Map<String, AttributeValue> result = scannerUserLastPasswordInfo(user).getItems().get(0);
            lastPassword = result.get("last_password").getS();
            log.info(lastPassword);
        } catch (Exception e) {
            log.error("specified key is not present");
        }
        Assert.assertTrue(StringUtils.isNotEmpty(lastPassword), user + " is not present");
    }

  @Then("^validate alarm of queue (.*) notification has status value (.*) into (.*) services$")
  public void validateAlarm(String queue, String status, String microservicesName) {
    String alarmName;
    String statusValue;
    QueueType queueType = getQueueType(queue);
    alarmName = buildAlarmNameByResource(microservicesName, queueType);
    statusValue = getStatusAlarm(status);

    boolean isAlarmThrow = isAlarmApproximateNumberOfMessagesVisible(alarmName,statusValue);
    Assert.assertTrue(isAlarmThrow, String.format("The alarm name %s is not Throw",alarmName));
  }


  @Then("^I wait during (.*) seconds before send the trigger of Alarm$")
  public void iWaitDuringSecondsBeforeSendTheTrigger(int timeInSec) {
    shortWait(timeInSec);
  }

  @Then("^validate (.*) the notification queue (.*) into (.*) is message available and messages in flight in (.*)$")
  public void validateNotificationQueueIsTrapped(String microservicesName, String queueType, String nameQueue, int value) {
      boolean isTrappedMessage= isTrappedMessagesQueue(microservicesName,queueType,nameQueue,value);
    Assert.assertTrue(isTrappedMessage,String.format("The notification queue %s is message available and messages in flight in %s",queueType,nameQueue));
  }

  @Then("^validate (.*) the notification queue (.*) into (.*) contain available message is different to (.*)$")
  public void validateMssNotificationQueueIsTrapped(String microservicesName, String queueType, String nameQueue, int value) {
    boolean isTrappedMessage= isTrappedMessagesQueue(microservicesName,queueType,nameQueue,value);
    Assert.assertFalse(isTrappedMessage,String.format("The MSS notification queue %s is message available and messages in flight in %s",queueType,nameQueue));
  }

  @Given("^if not Exist Transaction for current period$")
  public void ifNotExistTransactionForCurrentPeriod() {
      if(response!=null && scenarioData!=null){
      boolean hasTransactions = Boolean.parseBoolean(getScenarioContextVariables("hasTransactions"));
     if(!hasTransactions){
       log.info("Has NOT transactions in this current period");
       Assert.assertTrue(true);
     }else{
       log.info("Has transactions in this current period");
     }
    }
  }
}
