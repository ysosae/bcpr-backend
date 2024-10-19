package config;

import static config.AbstractAPI.saveInErrorPaymentsContext;
import static config.CognitoAWS.getSubIdByUsernameIntoCognito;
import static config.RestAssuredExtension.amazonDynamoDB;
import static config.RestAssuredExtension.getDefaultUsername;
import static config.RestAssuredExtension.getSessionUser;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.Select;
import enums.DynamoDBTable;
import enums.ProjectName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.SkipException;

public class DynamoDbAWS {
  private static final Logger log = Logger.getLogger(DynamoDbAWS.class);
  private static String INDEX_NAME = "subId-createdOn-index";
  private static final String KEY_CONDITION_EXPRESSION = "subId = :subId";
  private static final String FILTER_EXPRESSION = "#username = :username";
  private static final Map<String, String> EXPRESSION_ATTRIBUTE_NAMES = Map.of("#username", "username");
  private static final String WALLET_KEY_CONDITION_EXPRESSION = "subId = :subId";
  private static final String WALLET_FILTER_EXPRESSION = "#status <> :deleted";
  private static final Map<String, String> WALLET_EXPRESSION_ATTRIBUTE_NAMES = Map.of("#status", "status");
  private static final String PAYMENT_KEY_CONDITION_EXPRESSION = "subId = :subId";
  private static final String PAYMENT_FILTER_EXPRESSION = "#status <> :errorPayment";
  private static final Map<String, String> PAYMENT_EXPRESSION_ATTRIBUTE_NAMES = Map.of("#status", "status");

  public static String keyConditionExpression;
  public static String filterExpression;
  public static Map<String, AttributeValue> exclusiveStartKey = null;
  public static int totalItem=0;
  public static RestAssuredExtension rest = new RestAssuredExtension();

  public static List<Map<String, AttributeValue>> performPagedWalletQuery(String subId) {
    Map<String, AttributeValue> expressionAttributeValues = Map.of(
      ":subId", new AttributeValue().withS(subId),
      ":deleted", new AttributeValue().withS("DELETED")
    );

    QueryRequest queryRequest = generateQueryRequest(
      ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet),
      INDEX_NAME,
      WALLET_KEY_CONDITION_EXPRESSION,
      WALLET_FILTER_EXPRESSION,
      WALLET_EXPRESSION_ATTRIBUTE_NAMES,
      expressionAttributeValues
    );

    return executeQueryWithPagination(queryRequest);
  }

  public static String performPagedErrorPaymentQuery(String subId) {
    Map<String, AttributeValue> expressionAttributeValues = Map.of(
      ":subId", new AttributeValue().withS(subId),
      ":errorPayment", new AttributeValue().withS("CONFIRMED")
    );

    QueryRequest queryRequest = generateQueryRequest(
      ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment),
      INDEX_NAME,
      PAYMENT_KEY_CONDITION_EXPRESSION,
      PAYMENT_FILTER_EXPRESSION,
      PAYMENT_EXPRESSION_ATTRIBUTE_NAMES,
      expressionAttributeValues
    );

    List<Map<String, AttributeValue>> results = executeQueryWithPagination(queryRequest);

    if (results.isEmpty()) {
      log.info("The query returned no records");
      throw new SkipException("The query returned no records");
    }

    Map<String, AttributeValue> firstItem = results.get(0);
    String status = firstItem.get("status").getS();
    saveInErrorPaymentsContext("status", status);
    return status;
  }

  private static List<Map<String, AttributeValue>> executeQueryWithPagination(QueryRequest queryRequest) {
    List<Map<String, AttributeValue>> results = new ArrayList<>();
    Map<String, AttributeValue> exclusiveStartKey = null;

    do {
      QueryResult queryResult = getQueryResult(queryRequest.withExclusiveStartKey(exclusiveStartKey));
      assert queryResult != null;
      results.addAll(queryResult.getItems());
      exclusiveStartKey = queryResult.getLastEvaluatedKey();
    } while (exclusiveStartKey != null && !exclusiveStartKey.isEmpty());

    log.info("Total items retrieved: " + results.size());
    return results;
  }

  public static List<Map<String, AttributeValue>> performPagedPaymentQuery(String subId) {
    String user = getDefaultUsername();
    Map<String, AttributeValue> expressionAttributeValues = Map.of(
      ":subId", new AttributeValue().withS(subId),
      ":username", new AttributeValue().withS(user)
    );

    QueryRequest queryRequest = generateQueryRequest(
      ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment),
      INDEX_NAME,
      KEY_CONDITION_EXPRESSION,
      FILTER_EXPRESSION,
      EXPRESSION_ATTRIBUTE_NAMES,
      expressionAttributeValues
    );

    return executeQueryWithPagination(queryRequest);
  }

  public static String findValueByQueryIntoPayment(String subId, String user, String accountId, String status){
    keyConditionExpression = "subId = :subId";
    filterExpression = "#accountId = :accountId and #status = :status and #username = :username";
    Map<String, String> expressionAttributeNames =
      Map.of("#accountId", "accountId",
        "#status", "status",
        "#username", "username");
    Map<String, AttributeValue> withExpressionAttributeValues =
      Map.of(":accountId", new AttributeValue().withS(accountId),
        ":subId", new AttributeValue().withS(subId),
        ":status", new AttributeValue().withS(status),
        ":username", new AttributeValue().withS(user));

    QueryRequest queryRequest = generateQueryRequest
      (ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment),
        INDEX_NAME,
        keyConditionExpression,
        filterExpression,
        expressionAttributeNames,
        withExpressionAttributeValues,
        exclusiveStartKey
      );

    QueryResult queryResult = getQueryResult(queryRequest);

    assert queryResult != null;
    for (Map<String, AttributeValue> item : queryResult.getItems()) {
      try {
        log.info(item.get("status").getS());
        log.info(item.toString());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
    return status;
  }

  public static String findValueByQueryIntoWallet(String subId, String accountId, String status){
    keyConditionExpression = "subId = :subId";
    filterExpression = "#accountId = :accountId and #status = :status";
    Map<String, String> expressionAttributeNames =
      Map.of("#accountId", "accountId",
        "#status", "status");
    Map<String, AttributeValue> withExpressionAttributeValues =
      Map.of(":accountId", new AttributeValue().withS(accountId),
        ":subId", new AttributeValue().withS(subId),
        ":status", new AttributeValue().withS(status));

    QueryRequest queryRequest = generateQueryRequest
      (ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet),
        INDEX_NAME,
        keyConditionExpression,
        filterExpression,
        expressionAttributeNames,
        withExpressionAttributeValues,
        exclusiveStartKey
      );

    QueryResult queryResult = getQueryResult(queryRequest);

    assert queryResult != null;
    for (Map<String, AttributeValue> item : queryResult.getItems()) {
      try {
        log.info(item.toString());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
    return status;
  }

  public static QueryRequest generateQueryRequest(String dbDynamoDbTableName, String indexName,
                                           String keyConditionExpression, String filterExpression,
                                           Map<String, String> expressionAttributeNames,
                                           Map<String, AttributeValue> withExpressionAttributeValues,
                                           Map<String, AttributeValue> exclusiveStartKey){

    return new QueryRequest()
      .withTableName(dbDynamoDbTableName)
      .withIndexName(indexName)
      .withKeyConditionExpression(keyConditionExpression)
      .withFilterExpression(filterExpression)
      .withExpressionAttributeNames(expressionAttributeNames)
      .withExpressionAttributeValues(withExpressionAttributeValues)
      .withScanIndexForward(false)
      .withExclusiveStartKey(exclusiveStartKey)
      .withSelect(Select.ALL_ATTRIBUTES);
  }

  private static QueryRequest generateQueryRequest(String tableName, String indexName, String keyConditionExpression,
                                                   String filterExpression, Map<String, String> expressionAttributeNames,
                                                   Map<String, AttributeValue> expressionAttributeValues) {
    return new QueryRequest()
      .withTableName(tableName)
      .withIndexName(indexName)
      .withKeyConditionExpression(keyConditionExpression)
      .withFilterExpression(filterExpression)
      .withExpressionAttributeNames(expressionAttributeNames)
      .withExpressionAttributeValues(expressionAttributeValues)
      .withScanIndexForward(false)
      .withSelect(Select.ALL_ATTRIBUTES);
  }


  public static QueryResult getQueryResult (QueryRequest queryRequest){
    try {
      if(queryRequest!= null){
       return amazonDynamoDB.query(queryRequest);
      }

    }catch (Exception e){
      log.error("The query request not found");
      throw new SkipException("The query request not found" + e.getMessage());
    }
    return null;
  }

  public static void deleteItemDynamoDB(String tableName, String itemName, String itemValue) {
    DeleteItemRequest deleteItemRequest =
      new DeleteItemRequest()
        .withTableName(tableName) // replace with your table name
        .withKey(
          Collections.singletonMap(
            itemName, new AttributeValue(itemValue))); // replace with your item key

    try {
      amazonDynamoDB.deleteItem(deleteItemRequest);
      log.info(
        String.format("Item %s with value %s it was deleted successfully", itemName, itemValue));
    } catch (Exception e) {
      log.error("Unable to delete item: " + e.getMessage());
    }
  }

  public static void deleteItemDynamoDB(String tableName, String itemName, Map<String, AttributeValue> primaryKey) {
    DeleteItemRequest deleteItemRequest =
      new DeleteItemRequest()
        .withTableName(tableName) // replace with your table name
        .withKey(primaryKey); // replace with your item key

    try {
      amazonDynamoDB.deleteItem(deleteItemRequest);
      log.info(
        String.format("Item %s with value %s it was deleted successfully", itemName, primaryKey));
    } catch (Exception e) {
      log.error("Unable to delete item: " + e.getMessage());
    }
  }

  public static Map<String, AttributeValue> getPrimaryKeyIntoListItems(Map<String, AttributeValue> item , String primaryKeyAttribute){
    Map<String, AttributeValue> primaryKey = new HashMap<>();
    if(item!=null && !item.isEmpty()){
        if (item.containsKey(primaryKeyAttribute)){
          primaryKey.put(primaryKeyAttribute, item.get(primaryKeyAttribute));
        }
      }

   return primaryKey;
  }

  public static void removeExistingClaimDynamoDB(String transactionId){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ClaimData);
    deleteItemDynamoDB(dbTableName, "transactionId", transactionId);
  }

  public static void removeExistingLoginAttemptsDynamoDB(String username){
    String dbTablaName= ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LoginAttempts);
    deleteItemDynamoDB(dbTablaName, "username", username);
  }

  public static void removeExistingLastPasswordDynamoDB(String username, String itemName){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);
    String idValue = getAttributeValueLastPasswordResultDynamoDb(username, itemName);
    deleteItemDynamoDB(dbTableName, itemName, idValue);
  }

  public static void removeExistingAllLastPasswordDynamoDB(String username, String itemName){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);
    List<Map<String, AttributeValue>> listItem= getResultAllLastPasswordScannerDynamoDb(username);

    if (listItem==null || listItem.isEmpty()) {
      log.info("No record exists");
      return;
    }
    do {
      String idValue = getAllAttributeValueLastPasswordResultDynamoDb(username, itemName);
      deleteItemDynamoDB(dbTableName, itemName, idValue);
      listItem = getResultAllLastPasswordScannerDynamoDb(username);
    } while (!Objects.requireNonNull(listItem).isEmpty());
  }

  public static void removeExistingWalletDynamoDB(String itemName, List<Map<String, AttributeValue> > result){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);
    for (Map<String, AttributeValue> item:
      result) {
      Map<String, AttributeValue> primaryKey =getPrimaryKeyIntoListItems(item, itemName);
      deleteItemDynamoDB(dbTableName, itemName, primaryKey);
    }
  }

  public static void removeExistingPaymentDynamoDB(String itemName, List<Map<String, AttributeValue> > result){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment);
    for (Map<String, AttributeValue> item:
      result) {
      Map<String, AttributeValue> primaryKey =getPrimaryKeyIntoListItems(item, itemName);
      deleteItemDynamoDB(dbTableName, itemName, primaryKey);
    }
  }

  public static ScanResult scannerUserLastPasswordInfo(String user) {
    rest.putExpressionAttributeValues("String", ":username", user);
    ScanResult result = scanResultLastPasswordDynamoDB("username = :username");
    log.info("Results Count is: " + result.getCount());
    return result;
  }

  public static ScanResult scannerUserWalletInfo(String user) {
    rest.putExpressionAttributeValues("String", ":username", user);
    ScanResult result = scanResultWalletDynamoDB("username = :username");
    log.info("Results Count is: " + result.getCount());
    return result;
  }

  public static ScanResult scanResultLastPasswordDynamoDB(String withFilterExpression){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword);
    return rest.ScanAction(
        dbTableName,
        withFilterExpression);
  }

  public static ScanResult scanResultWalletDynamoDB(String withFilterExpression){
    String dbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet);
    return rest.ScanAction(
      dbTableName,
      withFilterExpression);
  }

  public static Map<String, AttributeValue> getResultFirstLastPasswordScannerDynamoDb(String username){
    try {
      return scannerUserLastPasswordInfo(username).getItems().get(0);
    }catch (Exception e) {
      log.error("specified key is not present");
    }
    return null;
  }

  public static List<Map<String, AttributeValue>> getResultAllLastPasswordScannerDynamoDb(String username){
    try {
      return scannerUserLastPasswordInfo(username).getItems();
    }catch (Exception e) {
      log.error("specified key is not present");
    }
    return null;
  }

  public static String getAttributeValueLastPasswordResultDynamoDb(String username, String itemName){
    Map<String, AttributeValue> result = getResultFirstLastPasswordScannerDynamoDb(username);
    if(result!=null && !result.isEmpty()){
      return result.get(itemName).getS();
    }
    return "";
  }

  public static String getAllAttributeValueLastPasswordResultDynamoDb(String username, String itemName){
    List<Map<String, AttributeValue>> ListResult = getResultAllLastPasswordScannerDynamoDb(username);
    assert ListResult != null;
    for (Map<String, AttributeValue> result:
      ListResult) {
      return result.get(itemName).getS();

    }
    return "";
  }

  public static List<Map<String, AttributeValue> > getListAttributeValueWalletResultDynamoDb(String username){
    String subId=  getSubIdByUsernameIntoCognito(username);
    List<Map<String, AttributeValue> > result = getAllResultWalletQueryDynamoDb(subId);
    if(result!=null && !result.isEmpty()){
      return result;
    }
     return null;
  }

  public static List<Map<String, AttributeValue> > getListAttributeValuePaymentResultDynamoDb(String username){
    String subId=  getSubIdByUsernameIntoCognito(username);
    List<Map<String, AttributeValue> > result = getAllResultPaymentQueryDynamoDb(subId);
    if(result!=null && !result.isEmpty()){
      return result;
    }
    return null;
  }




  public static QueryResult getAllWalletQueryBySubId(String subId){
    INDEX_NAME = "subId-createdOn-index";
    keyConditionExpression = "subId = :subId";
    Map<String, AttributeValue> withExpressionAttributeValues =
      Map.of(
          ":subId", new AttributeValue().withS(subId)
        );

    QueryRequest queryRequest = generateQueryRequest
      (ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet),
        INDEX_NAME,
        keyConditionExpression,
        withExpressionAttributeValues,
        exclusiveStartKey
      );

    QueryResult queryResult = getQueryResult(queryRequest);

    assert queryResult != null;
    for (Map<String, AttributeValue> item : queryResult.getItems()) {
      try {
        log.info(item.size());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
    return queryResult;
  }

  public static QueryResult getAllPaymentQueryBySubId(String subId){
    INDEX_NAME = "subId-createdOn-index";
    keyConditionExpression = "subId = :subId";
    Map<String, AttributeValue> withExpressionAttributeValues =
      Map.of(
        ":subId", new AttributeValue().withS(subId)
      );

    QueryRequest queryRequest = generateQueryRequest
      (ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment),
        INDEX_NAME,
        keyConditionExpression,
        withExpressionAttributeValues,
        exclusiveStartKey
      );

    QueryResult queryResult = getQueryResult(queryRequest);

    assert queryResult != null;
    for (Map<String, AttributeValue> item : queryResult.getItems()) {
      try {
        log.info(item.size());
      } catch (NullPointerException e) {
        log.error("specified key is not present");
      }
    }
    return queryResult;
  }

  public static QueryRequest generateQueryRequest(String dbDynamoDbTableName, String indexName,
                                                  String keyConditionExpression,
                                                  Map<String, AttributeValue> withExpressionAttributeValues,
                                                  Map<String, AttributeValue> exclusiveStartKey){

    return new QueryRequest()
      .withTableName(dbDynamoDbTableName)
      .withIndexName(indexName)
      .withKeyConditionExpression(keyConditionExpression)
      .withFilterExpression(filterExpression)
      .withExpressionAttributeValues(withExpressionAttributeValues)
      .withScanIndexForward(false)
      .withExclusiveStartKey(exclusiveStartKey)
      .withSelect(Select.ALL_ATTRIBUTES);
  }

  public static String getItemValue(String itemName) {
    return StringUtils.equalsIgnoreCase(itemName, "username") ? getSessionUser() : null;
  }

  public static void handleLastPasswordTable(String itemName) {
    String lastPassword = getAttributeValueLastPasswordResultDynamoDb(itemName, "last_password");
    log.info(lastPassword);
    removeExistingLastPasswordDynamoDB(itemName, "id");
  }

  public static void handleWalletTable(String itemName) {
    List<Map<String, AttributeValue>> result = getListAttributeValueWalletResultDynamoDb(itemName);
    if (result == null) {
      log.info(String.format("The query did not return any results in the Wallet table for user %s", itemName));
    } else {
      log.info(String.format("Records found in the Wallet table for user %s -> %s ", itemName, result.size()));
      removeExistingWalletDynamoDB("accountId", result);
    }
  }

  public static void handlePaymentTable(String itemName) {
    List<Map<String, AttributeValue>> result = getListAttributeValuePaymentResultDynamoDb(itemName);
    if (result == null) {
      log.info(String.format("The query did not return any results in the Payment table for user %s", itemName));
    } else {
      log.info(String.format("Records found in the Payment table for user %s -> %s ", itemName, result.size()));
      removeExistingPaymentDynamoDB("transactionId", result);
    }
  }

  public static void handleDefaultCase(String nameTable, String itemName, String itemValue) {
    String prefix = ResourcesAWS.buildPrefixResources(ProjectName.BCPR);
    deleteItemDynamoDB(prefix.concat(String.format("-%s", nameTable)), itemName, itemValue);
  }

  public static List<Map<String, AttributeValue> > getAllResultWalletQueryDynamoDb(String subId){
    try {
      return performPagedWalletQuery(subId);
    }catch (Exception e) {
      log.error("specified key is not present");
    }
    return null;
  }

  public static List<Map<String, AttributeValue> > getAllResultPaymentQueryDynamoDb(String subId){
    try {
      return performPagedPaymentQuery(subId);
    }catch (Exception e) {
      log.error("specified key is not present");
    }
    return null;
  }
}
