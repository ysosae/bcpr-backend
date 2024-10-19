package test;

import static config.DynamoDbAWS.deleteItemDynamoDB;
import static config.DynamoDbAWS.findValueByQueryIntoPayment;
import static config.DynamoDbAWS.findValueByQueryIntoWallet;
import static config.DynamoDbAWS.performPagedErrorPaymentQuery;
import static config.DynamoDbAWS.performPagedPaymentQuery;
import static config.DynamoDbAWS.performPagedWalletQuery;
import static config.DynamoDbAWS.scannerUserLastPasswordInfo;
import static config.DynamoDbAWS.scannerUserWalletInfo;
import static config.RestAssuredExtension.amazonDynamoDB;
import static config.RestAssuredExtension.getSessionUser;
import static config.RestAssuredExtension.getSubIdByUsernameIntoLocalData;
import static config.RestAssuredExtension.subId;
import static config.RestAssuredPropertiesConfig.getAwsBasicSessionCredentials;
import static storage.ScenarioContext.getScenarioContextVariables;
import static storage.ScenarioContext.saveInScenarioContext;
import static utils.AppDateFormats.getTodayDate;
import static utils.DataGenerator.randomAmount;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import config.AbstractAPI;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import enums.DynamoDBTable;
import enums.StatusPayments;
import features.commons.CommonsStepDefs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import software.amazon.awssdk.regions.Region;

public class DynamoDBTest extends AbstractAPI {
  private static final Logger log = Logger.getLogger(DynamoDBTest.class);

  public static String dbDynamoDbTableName = null;
  public static int currentValue = 8187;

  @Test
  public void testDynamoGetUser() {
    rest.putExpressionAttributeValues("String", ":username", getSessionUser());
    rest.putExpressionAttributeValues("String", ":creationDate", getTodayDate());
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);


    ScanResult result =
      rest.ScanAction(
        dbDynamoDbTableName,
        "username = :username and creationDate <= :creationDate");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        log.info(item.get("last_password").getS());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
  }

  @Ignore
  @Test
  public void testDynamoWithFiltersPhoneCode() {
    rest.putExpressionAttributeValues("String", ":ssn", "580666988");
    rest.putExpressionAttributeValues("String", ":status_", "DATA_VALIDATED");
    rest.putExpressionAttributeValues("String", ":expirationCode", getTodayDate());
    rest.putExpressionAttributeValues("String", ":type", "SSN");

    RestAssuredExtension.putExpressionAttributeNames("#data", "data");
    RestAssuredExtension.putExpressionAttributeNames("#status", "status");
    RestAssuredExtension.putExpressionAttributeNames("#type", "type");
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.EnrollmentData);
    ScanResult result =
      rest.ScanAction(
        dbDynamoDbTableName,
        "#data.socialSecurityNumber = :ssn and #status = :status_ and #type = :type and codeExpiration >= :expirationCode");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        log.info(item.get("phoneCode").getN());
        Map<String, AttributeValue> getData;
        getData = item.get("data").getM();
        log.info(getData.get("socialSecurityNumber").getS());
        log.info(item.get("data").getM().get("socialSecurityNumber").getS());
      } catch (NullPointerException e) {
        log.info("specified key is not present");
      }
    }
  }

  @Test
  public void testPopulatingWalletTableDynamoDB() {
    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    DynamoDB dynamoDBClient = new DynamoDB(amazonDynamoDB);
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);
    Table table = dynamoDBClient.getTable(dbDynamoDbTableName);
    int count = 0;
    while (count < 1) {
      String autoIncrementalKey = generateAutoIncrementalKey();
      log.info("autoIncrementalKey " + autoIncrementalKey);
      Item item = new Item()
        .withPrimaryKey("accountId", generateUUID())
        .withString("accountNumber", "1111222233330000")
        .withString("accType", "s")
        .withString("createdOn", getTodayDate())
        .withString("customName", "QA-Performance")
        .withString("routing", "110000000")
        .withString("status", "CONFIRMED")
        .withString("subId", getSubIdByUsernameIntoLocalData("sub"));


      try {
        table.putItem(item);
        log.info("Item added to DynamoDB table." + item.toString());
      } catch (Exception e) {
        log.error("Error during DynamoDB put item: " + e.getMessage());
      }

      count++;
    }
  }

  public static String generateAutoIncrementalKey() {
    return String.valueOf(++currentValue);
  }

  @Test
  public void testPopulatingPaymentTableDynamoDB() {
    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    DynamoDB dynamoDBClient = new DynamoDB(amazonDynamoDB);
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);
    Table table = dynamoDBClient.getTable(dbDynamoDbTableName);

    int count = 0;
    while (count < 1) {

      Item item = new Item()
        .withPrimaryKey("transactionId", generateUUID())
        .withString("accountId", generateUUID())
        .withString("amount", randomAmount())
        .withString("cardId", "U2FsdGVkX18se8nzbyai7NpqkUINSupWCG95pjobiDXwBUtf0+nr0O2nwTgFB62y")
        .withString("createdOn", getTodayDate())
        .withString("institutionId", "659")
        .withString("status", "CONFIRMED")
        .withString("subId", getSubIdByUsernameIntoLocalData("sub"))
        .withString("username", "Yuliet2023");


      try {
        table.putItem(item);
        log.info("Item added to DynamoDB table." + item.toString());
      } catch (Exception e) {
        log.error("Error during DynamoDB put item: " + e.getMessage());
      }

      count++;
    }
  }

  @Test
  public void testDeleteItemBlockReasonDynamoDB() {
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.BlockReason);
    String itemName = "username";
    String itemValue = "Yuliet2023";

    DeleteItemRequest deleteItemRequest =
      new DeleteItemRequest()
        .withTableName(dbDynamoDbTableName)
        .withKey(
          Collections.singletonMap(
            itemName, new AttributeValue(itemValue)));

    try {
      amazonDynamoDB.deleteItem(deleteItemRequest);
      log.info(
        String.format("Item %s with value %s it was deleted successfully", itemName, itemValue));
    } catch (Exception e) {
      log.error("Unable to delete item: " + e.getMessage());
    }
  }

  @Test
  public void testDeleteItemLastPasswordDynamoDB() {
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);

    String itemName = "id";
    String itemValue = "Automation2023";

    rest.putExpressionAttributeValues("String", ":username", itemValue);
    Map<String, AttributeValue> result = scannerUserLastPasswordInfo(itemValue).getItems().get(0);
    String lastPassword = result.get("last_password").getS();
    log.info(lastPassword);
    String id = result.get("id").getS();

    DeleteItemRequest deleteItemRequest =
      new DeleteItemRequest()
        .withTableName(dbDynamoDbTableName)
        .withKey(
          Collections.singletonMap(
            itemName, new AttributeValue(id)));

    try {
      amazonDynamoDB.deleteItem(deleteItemRequest);
      log.info(
        String.format("Item %s with value %s it was deleted successfully", itemName, itemValue));
    } catch (Exception e) {
      log.error("Unable to delete item: " + e.getMessage());
    }
  }

  @Test
  public void testDeleteItemWalletDynamoDB() {
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);

    String itemName = "subId";
    String itemValue = "55b58d3f-7f17-456f-b718-af227c3c2861";

    rest.putExpressionAttributeValues("String", ":username", itemValue);

    String SubId= getSubIdByUsernameIntoLocalData("sub");
    performPagedWalletQuery(SubId);
    Map<String, AttributeValue> result = scannerUserWalletInfo(itemValue).getItems().get(0);
    String lastPassword = result.get("last_password").getS();
    log.info(lastPassword);
    String id = result.get("id").getS();

    DeleteItemRequest deleteItemRequest =
      new DeleteItemRequest()
        .withTableName(dbDynamoDbTableName)
        .withKey(
          Collections.singletonMap(
            itemName, new AttributeValue(id)));

    try {
      amazonDynamoDB.deleteItem(deleteItemRequest);
      log.info(
        String.format("Item %s with value %s it was deleted successfully", itemName, itemValue));
    } catch (Exception e) {
      log.error("Unable to delete item: " + e.getMessage());
    }
  }


  @Test
  public void testDynamoWithFiltersBlockReason() {
    String user = "Yuliet2023";
    rest.putExpressionAttributeValues("String", ":username", user);
    RestAssuredExtension.putExpressionAttributeNames("#username", "username");
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.BlockReason);

    ScanResult result =
      rest.ScanAction(dbDynamoDbTableName, "#username = :username");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> ignored : result.getItems()) {
      try {
        log.info(result.getItems().get(0).get("blockReason").getS());
        saveInScenarioContext("blockReason", result.getItems().get(0).get("blockReason").getS());
        compareValueStringWithScenarioData("card-activation", "SCENARIO_DATA:blockReason");
        dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.BlockReason);
        deleteItemDynamoDB(dbDynamoDbTableName, "username", user);
      } catch (Exception e) {
        log.error("specified key is not present " + e.getMessage());
      }
    }
  }

  @Test
  public void testDynamoWithFiltersPaymentsStatusByAccountId() {
    String user = "Facundo2023";
    String accountId = "8185";
    rest.putExpressionAttributeValues("String", ":username", user);
    rest.putExpressionAttributeValues("String", ":accountId", accountId);


    RestAssuredExtension.putExpressionAttributeNames("#username", "username");
    RestAssuredExtension.putExpressionAttributeNames("#accountId", "accountId");
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);

    ScanResult result =
      rest.ScanAction(dbDynamoDbTableName, "#username = :username and #accountId = :accountId");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> ignored : result.getItems()) {
      try {
        log.info(result.getItems().get(0).get("status").getS());
        saveInScenarioContext("statusPayments", result.getItems().get(0).get("status").getS());
      } catch (NullPointerException e) {
        log.info("specified key is not present");
      }
    }

    compareValueStringWithScenarioData("INVALID_FIELD_RECEIVED_ROUTINGNUMBER",
      "SCENARIO_DATA:statusPayments");

  }

  @Test
  public void testDynamoWithFiltersPaymentsStatusByUser() {
    String user = "Yuliet2023";
    List<StatusPayments> enumStatusPaymentsList = new ArrayList<>();
    enumStatusPaymentsList.add(StatusPayments.ERROR_ROUTING_MUST_BE_9);
    enumStatusPaymentsList.add(StatusPayments.AUTHENTICATION_ERROR);
    enumStatusPaymentsList.add(StatusPayments.INTERNAL_SERVER_ERROR);
    enumStatusPaymentsList.add(StatusPayments.INVALID_FIELD_RECEIVED_ROUTINGNUMBER);
    enumStatusPaymentsList.add(StatusPayments.INVALID_IP_320921283);
    enumStatusPaymentsList.add(StatusPayments.PENDING_CONFIRMATION);

    rest.putExpressionAttributeValues("String", ":username", user);

    RestAssuredExtension.putExpressionAttributeNames("#username", "username");
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);

    ScanResult result =
      rest.ScanAction(dbDynamoDbTableName, "#username = :username");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> ignored : result.getItems()) {
      try {
        log.info(result.getItems().get(0).get("status").getS());
        saveInScenarioContext("statusPayments", result.getItems().get(90).get("status").getS());
        saveInErrorPaymentsContext("statusErrorPayments",
          result.getItems().get(90).get("status").getS());
      } catch (NullPointerException e) {
        log.info("specified key is not present");
      }
    }
    for (StatusPayments status :
      enumStatusPaymentsList) {
      if (StringUtils.equalsIgnoreCase(getScenarioContextVariables("statusPayments"), status.name())
        && StringUtils.equalsIgnoreCase(getScenarioContextVariables("statusPayments"),
        StatusPayments.CONFIRMED.name())) {
        saveInScenarioContext("statusPayments", status.name());
        break;
      }
    }
    compareValueStringWithScenarioData("ERROR_PAYMENTS:statusErrorPayments",
      "SCENARIO_DATA:statusPayments");
  }


  @Ignore
  @Test
  public void testDynamoWithFiltersEnrollment() {
    String enrollmentID = "67772631-8cfc-44ac-b7fd-845ec670ac81";
    QuerySpec keyConditionExpression = rest.querySpec(null, "enrollmentId", enrollmentID);
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.EnrollmentData);
    rest.performQuery(dbDynamoDbTableName, keyConditionExpression);
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.CardActivationAttempts);
    ScanResult result =
      rest.ScanAction(
        dbDynamoDbTableName,
        "#username = :username and #attempts = :attempts ");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        log.info(item.get("attempts").getN());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
  }

  @Ignore
  @Test
  public void testDynamoWithFiltersCardActivationAttempts() {
    String user = "yoharasantana";
    rest.putExpressionAttributeValues("String", ":username", user);
    RestAssuredExtension.putExpressionAttributeNames("#username", "username");

    ScanResult result =
      rest.ScanAction("BCPR-DEV-IMP-CardActivationAttempts", "#username = :username");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        log.info(item.get("attempts").getN());
        saveInScenarioContext("attempts", item.get("attempts").getN());
      } catch (NullPointerException e) {
        log.info("specified key is not present");
      }
    }

    compareValueWithScenarioData(3, "SCENARIO_DATA:attempts");
  }

  @Test
  public void testGetQueryAllDynamoBdWallet() {

    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);
    String SubId= getSubIdByUsernameIntoLocalData("sub");
    performPagedWalletQuery(SubId);

  }

  @Test
  public void testGetQueryFiltersDynamoBdWalletByAccountId() {
    String accountId = "11a2b03f-4d12-4b70-a31a-0bfaa6c7baa5";
    String status= "CONFIRMED";

    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);
    String subId= getSubIdByUsernameIntoLocalData("sub");
    findValueByQueryIntoWallet(subId, accountId, status);

  }

  @Test
  public void testGetQueryAllPayment() {
    RestAssuredExtension.setSessionUser("facundo");
    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);
    String SubId= getSubIdByUsernameIntoLocalData("sub");
    log.info(subId);
    performPagedPaymentQuery(SubId);

  }

  @Test
  public void testFindStatusOfPayment() {
    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    String statusFilter="";
    String status = "CONFIRMED";
    String user = "Yuliet2023";
    String accountId = "11a2b03f-4d12-4b70-a31a-0bfaa6c7baa5";
    String SubId= getSubIdByUsernameIntoLocalData("sub");

    if(StringUtils.equalsIgnoreCase(status,"CONFIRMED")){
      saveInScenarioContext("status", status);
      if (scenarioResponse.has("accountIdPayments")) {
        accountId = scenarioData.get("accountIdPayments").toString();
      }
      statusFilter = findValueByQueryIntoPayment(SubId, user, accountId, status);
    }
    if(StringUtils.equalsIgnoreCase(status,"ERROR_PAYMENTS")){
      saveInErrorPaymentsContext("status", status);
      statusFilter = performPagedErrorPaymentQuery(SubId);
    }

    log.info(statusFilter);
  }

  @Test
  public void testFindErrorStatusOfPayment() {
    amazonDynamoDB =
      AmazonDynamoDBClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);
    String SubId= getSubIdByUsernameIntoLocalData("sub");
    performPagedErrorPaymentQuery(SubId);

  }



  @Test
  public void testGetScanAllDynamoBdWallet() {
    rest.putExpressionAttributeValues("String", ":username", getSessionUser());
    rest.putExpressionAttributeValues("String", ":creationDate", getTodayDate());
    dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);

    ScanResult result =
      rest.ScanAction(
        dbDynamoDbTableName,
        "username = :username and creationDate <= :creationDate");
    log.info("Results Count is: " + result.getCount());

    for (Map<String, AttributeValue> item : result.getItems()) {
      try {
        log.info(item.get("last_password").getS());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
  }

  @Test
  public void testGetScanAllDynamoBdLastPassword() {
    CommonsStepDefs commonsStepDefs= new CommonsStepDefs();
    commonsStepDefs.deleteItemDynamoDbByTableNameAndItemName("ALL", "LastPassword", "Automation2023");
  }

  @Test
  public void testDeleteWalletBySubId() {
    CommonsStepDefs commonsStepDefs = new CommonsStepDefs();
    commonsStepDefs.deleteItemDynamoDbByTableNameAndItemName("Wallet", "Patria2023");

  }

}
