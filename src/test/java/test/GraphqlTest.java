package test;

import static config.ResourcesAWS.servicesClientAWS;
import static config.RestAssuredExtension.*;
import static config.RestAssuredPropertiesConfig.getEnvironment;
import static config.ServicesClientAWS.apiGatewayClient;
import static org.testng.Assert.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonLambdaFrontendAPIKeyValueConstant;
import config.AbstractAPI;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import enums.StatusCard;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import model.RewardsValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.ApiGatewayException;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import storage.RewardsStorage;

public class GraphqlTest extends AbstractAPI {
    private static final Logger log = Logger.getLogger(GraphqlTest.class);
    public static JsonPath iteratorList;
    public static Response responseList;
    public static ResponseOptions<Response> response = null;
    private static Integer pos = 0;
    private static Integer size = 0;
//    public static ApiGatewayClient apiGatewayClient = servicesClientAWS.getApiGatewayClient();
    ResourcesAWS awsResources= new ResourcesAWS();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

//  @Test
//  public void getApiGatewayGraphql() {
//    String env = awsResources.getPrefix();
//    GetRestApisResponse restApiEnv = apiGatewayClient.getRestApis();
//    RestApi restApi;
//    try {
//      if (restApiEnv.hasItems()) {
//        restApi = restApiEnv.items().stream().filter(func ->
//            StringUtils.equalsIgnoreCase(
//              func.name(), String.format("%s-Frontend-API-Gateway", env)))
//          .findFirst()
//          .orElse(null);
//
//        assert restApi != null;
//        String apiId = restApi.id();
//        log.info(String.format("%s.execute-api.us-east-1.amazonaws.com", apiId));
//      } else {
//        throw new SkipException("Api Gateway didn't contains name available");
//      }
//    } catch (ApiGatewayException e) {
//      throw new SkipException(e.getMessage());
//    }
//  }

  @Test
  public void testGetQueryListClaimTypes() {
    responseList = postMethodGraphQLGetList("graphQL/listClaimTypes.graphql");
    try {
      responseList.getBody().prettyPrint();
      iteratorList = new JsonPath(responseList.asString());
      int s = iteratorList.getInt("data.listClaimTypes.size()");
      for (int i = 0; i < s; i++) {
        String id = iteratorList.getString("data.listClaimTypes[" + i + "].id");
        String title = iteratorList.getString("data.listClaimTypes[" + i + "].title");
        String description = iteratorList.getString("data.listClaimTypes[" + i + "].description");
        String isCardLockedByFraud =
          iteratorList.getString("data.listClaimTypes[" + i + "].isCardLockedByFraud");
        String details =
          iteratorList.getString("data.listClaimTypes[" + i + "].details[" + i + "]");
        log.info("CC: " + (i + 1) + " -> Id: " + id);
        log.info("CC: " + (i + 1) + " -> title: " + title);
        log.info("CC: " + (i + 1) + " -> description: " + description);
        log.info("CC: " + (i + 1) + " -> isCardLockedByFraud: " + isCardLockedByFraud);
        log.info("CC: " + (i + 1) + " -> details: " + details);
      }

    } catch (NullPointerException e) {
      log.error("Path is invalid" + e.getMessage());
    }
  }

  @Test
  public void testGetQueryListFAQS() {
    responseList = postMethodGraphQLGetList("graphQL/getListFAQsByEnglishLanguage.graphql");
    try {
      responseList.getBody().prettyPrint();
      iteratorList = new JsonPath(responseList.asString());
      int s = iteratorList.getInt("data.listFAQS.size()");
      for (int i = 0; i < s; i++) {
        log.info(iteratorList.getString("data.listFAQS[" + i + "].questionId"));
        log.info(iteratorList.getString("data.listFAQS[" + i + "].question"));
        log.info(iteratorList.getString("data.listFAQS[" + i + "].answer"));
        log.info(iteratorList.getString("data.listFAQS[" + i + "].category"));
      }

    } catch (NullPointerException e) {
      log.error("Path is invalid" + e.getMessage());
    }
  }

  @Test
  public void testGetClaimTypesFilter() {
    String description = "";
    JsonObject details = new JsonObject();

    try {
      response = postMethodGraphQL("graphQL/listClaimTypes.graphql");
      JsonArray list = JsonParser.parseString(response.getBody().asString()).getAsJsonObject().getAsJsonArray("data").getAsJsonObject().getAsJsonArray("listClaimTypes");

      JsonElement dataElement = list.asList().stream()
        .filter(dataEntry -> StringUtils.containsIgnoreCase(dataEntry.toString(), "ATM did not issue the money"))
        .findFirst()
        .orElse(null);

      if (dataElement != null && dataElement.isJsonObject()) {
        JsonObject dataClaim = dataElement.getAsJsonObject();
        description = dataClaim.has("description") ? dataClaim.get("description").getAsString() : "Description not found";

        JsonArray detailsArray = dataClaim.has("details") ? dataClaim.getAsJsonArray("details") : new JsonArray();
        if (!detailsArray.isEmpty()) {
          details = detailsArray.get(0).getAsJsonObject();
        }

        detailsProcessing(dataClaim);
      } else {
        log.warn("No claim data found with the specified criteria.");
      }
    } catch (Exception e) {
      log.error("An error occurred while processing the claim types filter:", e);
    }

    log.info("Description: {}" + description);
    log.info("Details: {}" + details.toString());
  }

  private void detailsProcessing(JsonObject dataClaim) {
    JsonArray detailsArray = dataClaim.has("details") ? dataClaim.getAsJsonArray("details") : new JsonArray();
    if (!detailsArray.isEmpty()) {
      for (JsonElement detailElement : detailsArray) {
        if (detailElement.isJsonObject()) {
          JsonObject detail = detailElement.getAsJsonObject();
          String type = detail.has("type") ? detail.get("type").getAsString() : "Type not found";
          log.info("Detail Type: {}" + type);
        }
      }
    } else {
      log.warn("No details found in the data claim.");
    }
  }

  @Test
  public void testGetListFAQS() {
   response = postMethodGraphQL("graphQL/getListFAQsByLanguage.graphql");

    try {
      JsonArray list = JsonParser.parseString(response.getBody().asString())
        .getAsJsonObject()
        .getAsJsonObject("data")
        .getAsJsonArray("listFAQS");

      list.forEach(this::DataFAQS);
    } catch (Exception e) {
      log.error("Path is invalid", e);
    }
  }

  public void DataFAQS(JsonElement dataEntry) {
    JsonObject data = null;
    try {
      data = dataEntry.getAsJsonObject();
    } catch (Exception e) {
      log.error("Error parsing dataEntry", e);
    }

    if (data != null) {
      String QUESTION = data.has("question") ? data.get("question").getAsString() : "";
      String ANSWER = data.has("answer") ? data.get("answer").getAsString().split("(?<=[,.])|(?=[,.])")[0] : "";
      JsonObject CATEGORIES = data.has("category") ? data.getAsJsonObject("category") : new JsonObject();
      String CATEGORY = CATEGORIES.has("description") ? CATEGORIES.get("description").getAsString() : "";

      log.info(QUESTION);
      log.info(ANSWER);
      log.info(CATEGORY);
    }
  }

  @Test
  public void testTransactionsDynamic() {
    String body = "getTransactionsPathActivated.txt";
    String variables = "getTransactionsPathActivatedValues.json";
    response = postMethodGraphQL(body, variables);
  }

  @Test
  public void testGetStatusCardList() {
    response = postMethodGraphQL("graphQL/listCreditsCards.graphql");

    try {
      JsonArray list = JsonParser.parseString(response.getBody().asString())
        .getAsJsonObject()
        .getAsJsonObject("data")
        .getAsJsonArray("listCreditsCards");

      size = list.size();
      list.forEach(dataEntry -> DataListStatus(dataEntry, StatusCard.ACTIVE));
    } catch (Exception e) {
      log.error("Path is invalid", e);
    }
  }

  public void DataListStatus(JsonElement dataEntry, StatusCard status) {
    JsonObject data;
    try {
      data = dataEntry.getAsJsonObject();
      if (isStatusCardPresent(status, data) && pos <= size) {
        log.info("Position of the card " + data.get("last4Digits").getAsString() +
          " with " + status + " status is " + pos);
      } else if (Objects.equals(pos, size)) {
        log.info("Does not exist in the carousel cards " + status);
      }
    } catch (Exception e) {
      log.error("Error processing card data", e);
    }
  }

  public boolean isStatusCardPresent(StatusCard status, JsonObject data) throws Exception {
    boolean visitStatus = false;
    switch (status) {
      case ACTIVE:
        if (data.has("isActive") && data.get("isActive").getAsBoolean() &&
          data.has("isCreditCardExpired") && !data.get("isCreditCardExpired").getAsBoolean() &&
          data.has("blockingStatus") && data.get("blockingStatus").getAsString().contains("NoBlocked")) {
          visitStatus = true;
          log.info("The card " + data.get("last4Digits").getAsString() + " is ACTIVE");
        }
        break;

      case BLOCK_TEMPORARY:
        if (data.has("blockingStatus") && data.get("blockingStatus").getAsString().contains("TemporaryBlocked")) {
          visitStatus = true;
          log.info("The card " + data.get("last4Digits").getAsString() + " is BLOCKED for FRAUD");
        }
        break;

      case EXPIRED:
        if (data.has("isCreditCardExpired") && data.get("isCreditCardExpired").getAsBoolean()) {
          visitStatus = true;
          log.info("The card " + data.get("last4Digits").getAsString() + " is EXPIRED");
        }
        break;

      case INACTIVE:
        if (data.has("isActive") && !data.get("isActive").getAsBoolean()) {
          visitStatus = true;
          log.info("The card " + data.get("last4Digits").getAsString() + " is INACTIVE");
        }
        break;

      case BLOCK_FRAUD:
        if (data.has("blockingStatus") && data.get("blockingStatus").getAsString().contains("FraudBlocked")) {
          visitStatus = true;
          log.info("The card " + data.get("last4Digits").getAsString() + " is BLOCKED for FRAUD");
        }
        break;

      default:
        throw new Exception("Did not find required data");
    }
    ++pos;
    return visitStatus;
  }


  @DataProvider(name = "users")
  public Object[][] users() {
    return new Object[][] {
      {"Yuliet2023"},
      {"Miquel2023"},
      {"Candido2023"},
      {"Edgardo2023"},
      {"Facundo2023"}
    };
  }

  @DataProvider(name = "body")
  public Object[][] body() {
    return new Object[][] {
      {"graphQL/getListFAQsByEnglishLanguage.graphql"},
      {"graphQL/getListFaqsForRewardsEnglish.graphql"},
      {"graphQL/listTransactionsWithSelectedDate.graphql"},
      {"graphQL/getCardActivationMutation.graphql"}
    };
  }

  @DataProvider(name = "bodyWallet")
  public Object[][] bodyWallet() {
    return new Object[][] {
      {"graphQL/addWalletAccount.graphql"},
      {"graphQL/revalidateWalletAccount.graphql"},
      {"graphQL/confirmWalletAccount.graphql"},
      {"graphQL/getListWalletAccounts.graphql"},
      {"graphQL/makePayment.graphql"},
      {"graphQL/findRecentPayments.graphql"},
      {"graphQL/updateWalletCustomName.graphql"},
      {"graphQL/markWalletAccountAsDeleted.graphql"},
    };
  }

  @DataProvider(name = "combinedData")
  public Object[][] combinedData() {
    Object[][] usersData = users();
    Object[][] bodyData = body();

    int totalLength = usersData.length * bodyData.length;

    Object[][] combined = new Object[totalLength][2];

    int index = 0;
    for (Object[] userData : usersData) {
      for (Object[] bodyDatum : bodyData) {
        if (index < totalLength) {
          combined[index][0] = userData[0];
          combined[index][1] = bodyDatum[0];
          index++;
        }
      }
    }
    return combined;
  }

  @DataProvider(name = "combinedDataWallet")
  public Object[][] combinedDataWallet() {
    Object[][] usersData = users();
    Object[][] bodyData = bodyWallet();

    int totalLength = usersData.length * bodyData.length;

    Object[][] combined = new Object[totalLength][2];

    int index = 0;
    for (Object[] userData : usersData) {
      for (Object[] bodyDatum : bodyData) {
        if (index < totalLength) {
          combined[index][0] = userData[0];
          combined[index][1] = bodyDatum[0];
          index++;
        }
      }
    }
    return combined;
  }

  @DataProvider(name = "combinedDataRewards")
  public Object[][] combinedDataRewards() {
    Object[][] usersData = users();
    Object[][] bodyData = bodyListCreditCards();

    int totalLength = usersData.length * bodyData.length;

    Object[][] combined = new Object[totalLength][2];

    int index = 0;
    for (Object[] userData : usersData) {
      for (Object[] bodyDatum : bodyData) {
        if (index < totalLength) {
          combined[index][0] = userData[0];
          combined[index][1] = bodyDatum[0];
          index++;
        }
      }
    }
    return combined;
  }


  @Test(dataProvider = "combinedData")
  public void testMutationQueryGraphql(String usr, String body) {
    setSessionUser(usr);
    response = RestAssuredExtension.postMethodGraphQL(body);
    response.getBody().prettyPrint();
    assertEquals(response.statusCode(), 200);
  }

  @Test
  public void testcardActivation2Files() {
    String usr = "yuliet";
    setSessionUser(usr);
    generateBearerToken(true);
    String cardData = "cardActivation_8456.json";
    response = RestAssuredExtension.postMethodGraphQL("cardActivation.txt", cardData);
    response.getBody().prettyPrint();
  }

  @Test
  public void testTransactionType() {
    response =
      postMethodMiddleware(
        "v1/transactions", "transactionCreateType.txt", "transactionCredit.json");
  }

  @Test
  public void testTransactionTypeOneFile() {
    setOperationType("CREDIT");
    response = postMethodMiddleware("v1/transactions", "transactionCreateType.txt");
  }

  @Test
  public void testSchemaValidator() {
    setSessionUser("yuliet");
    response = RestAssuredExtension.postMethodGraphQL("graphQL/listInstitutions.graphql");
    rest.matchesJsonSchemaValidator(response, "data/schemas/graphQL/listInstitutionsOutput.json");
  }

  @Test
  public void getObjectListCreditCards() {
    setSessionUser("yuliet");
    response = RestAssuredExtension.postMethodGraphQL("graphQL/listCreditsCards.graphql");
    String jsonString = response.getBody().asString();
    JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

    try {
      JsonElement availableCreditElement = jsonObject.get("data").getAsJsonObject().get("listCreditCards").getAsJsonObject().get("availableCredit");

      if (availableCreditElement.isJsonPrimitive()) {
        if (availableCreditElement.getAsJsonPrimitive().isString()) {
          log.info("availableCredit is a string");
        } else if (availableCreditElement.getAsJsonPrimitive().isNumber()) {
          if (availableCreditElement.getAsJsonPrimitive().isNumber()) {
            log.info("availableCredit is an integer");
          } else {
            log.info("availableCredit is a float");
          }
        }
      }

      log.info("availableCredit: " + availableCreditElement);
    } catch (Exception e) {
      log.error("Error processing JSON", e);
    }
  }

  @Test
  public void ListQuery() {
    for (String query : listQueryBCPR()) {
      log.info(query);
    }

  }

  @Test
  public void listMutation() {
    for (String mutation : listMutationBCPR()) {
      log.info(mutation);
    }
  }

  public static List<String> listQueryBCPR() {
    return List.of(
      "listCreditsCards",
      "listCallCenters",
      "listClaimTypes",
      "listContactUsReason",
      "listFAQS",
      "listFaqsForRewards",
      "minimumMobileVersionAllow",
      "healthCkeck",
      "listInstitutions",
      "listCustomerInstitutions",
      "login",
      "refreshToken",
      "logout",
      "findRecentPayments",
      "getProfile",
      "listTransactions",
      "listInProcessTransactions",
      "listWalletAccounts",
      "getS3UploadUrl"
    );
  }

  public static List<String> listMutationBCPR() {
    return List.of(
      "switchCardTemporaryBlockStatus",
      "cardActivation",
      "submitClaim",
      "closeClaim",
      "sendLoggedContactUsRequest",
      "sendContactUsRequest",
      "enrollmentValidation",
      "codeValidation",
      "enrollUser",
      "resendCode",
      "passwordUpdate",
      "makePayment",
      "setNewPassword",
      "regainAccessCodeValidation",
      "resendRegainAccessCode",
      "confirmWalletAccount",
      "markWalletAccountAsDeleted",
      "addWalletAccount",
      "revalidateWalletAccount",
      "accessTokenWalletAccount",
      "updateWalletCustomName",
      "sendSMSNotification",
      "sendEmailNotification"
    );
  }


  @Test(dataProvider = "users")
  public void testGetListWallet(String usr) {
    try {
      getTokenByUser(usr);
      setTokenAndClean(usr);
      response = RestAssuredExtension.postMethodGraphQL("getListWalletAccounts.txt");
      int length =getSizeResponseGraphql("data.listWalletAccounts");
      log.info("RESPONSE GRAPHQL: " + length);
    } catch (NullPointerException e) {
      log.error("Path is invalid" + e.getMessage());
    }
  }

  @Test
  public void testConcurrenceUserWallet() {
    RestAssured.baseURI = "https://rdwrptsmxf.execute-api.us-east-1.amazonaws.com/qa/graphql";
    int numberOfUsers = 5;
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
    List<Future<Response>> futures = new ArrayList<>();
    for (int i = 0; i < numberOfUsers; i++) {
      Callable<Response> task = () -> (Response) postMethodGraphQL("graphQL/getListWalletAccounts.graphql");
      Future<Response> future = executorService.submit(task);
      futures.add(future);
    }
    List<Response> responses = new ArrayList<>();
    for (Future<Response> future : futures) {
      try {
        Response response = future.get();
        responses.add(response);
      } catch (InterruptedException | ExecutionException e) {
       log.error(e.getMessage());
      }
    }

    executorService.shutdown();

    for (int i = 0; i < responses.size(); i++) {
      log.info("User " + (i + 1) + " response status code: " + responses.get(i).getStatusCode());

    }
  }

  @Test(dataProvider = "combinedDataWallet")
  public void testAddWallet(String usr, String body) {
    try {
      String key = "accountIdPayments";
      String path = "data.addWalletAccount.accountId";
      getTokenByUser(usr);
      setTokenAndClean(usr);
      response = RestAssuredExtension.postMethodGraphQL(body);

      updateScenarioData(key, response.getBody().jsonPath().getString(path));

      log.info(String.format("The key %s was stored with value %s%n", key, response.getBody().jsonPath().getString(path)));
      log.info(response.getBody().prettyPrint());
    } catch (NullPointerException e) {
      log.error("Path is invalid: " + e.getMessage());
    }
  }

  @DataProvider(name = "bodyListCreditCards")
  public Object[][] bodyListCreditCards() {
    return new Object[][] {
      {"graphQL/listCreditsCards.graphql"}
    };
  }

  @Test
  public void testGetCallCenterObjectValidation() {
    validateIVRCallCenter();
  }

  @Test(dataProvider = "combinedDataRewards")
  public void testRewardsValidator(String usr, String body) {
    setSessionUser(usr);
    response = RestAssuredExtension.postMethodGraphQL(body);

    response.getBody().prettyPrint();

    saveScenarioDataWithKeyAndPath("rewardsPoints", "data.listCreditsCards[0].rewards.rewardsPoints");
    saveScenarioDataWithKeyAndPath("canRedeemPoints", "data.listCreditsCards[0].rewards.canRedeemPoints");
    RewardsStorage rewardsStorage = new RewardsStorage();
    RewardsValidator rewardsValidator = new RewardsValidator(rewardsStorage,
      Integer.parseInt(CommonLambdaFrontendAPIKeyValueConstant.THRESHOLD_REWARDS_LIMIT) );
    rewardsValidator.validateRewards();
  }

  private static void updateScenarioData(String key, String value) {
    scenarioData.addProperty(key, value);
  }

  private static void saveScenarioDataWithKeyAndPath(String key, String path) {
    String value = response.getBody().jsonPath().getString(path);
    updateScenarioData(key, value);
    log.info(String.format("The key %s was stored with value %s%n", key, value));
  }

    @Test
    public void executeLoginPRD() {

        try {

//            response = RestAssuredExtension.postMethodGraphQL("src/test/resources/data/loginPRD.graphql");
            RestAssured.given()
                    .config(RestAssured.config()
                            .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                                    .setParam("http.connection.timeout", 80000) // Connection timeout in milliseconds
                                    .setParam("http.socket.timeout", 80000))); // Socket read timeout in milliseconds
            setDefaultHeaders();
            setBodyGraphql("graphQL/loginPRD.graphql");
            try {
                builder.setBaseUri("https://bcpr-api.evertecinc.com/ah1hz2h2fi.execute-api.us-east-1.amazonaws.com");

                request = RestAssured.given().spec(builder.build());
                response = request.post(new URI("/prd"));

            } catch (URISyntaxException e) {
                log.error(e.getMessage());
            }
            log.info("Response Success:" + response.statusCode());
        } catch (NullPointerException e) {
            log.error("Path is invalid: " + e.getMessage());
        }
    }

    public void startScheduledTests() {
        scheduler.scheduleAtFixedRate(() -> {
            executeLoginPRD();
        }, 0, 3, TimeUnit.MINUTES);
    }

    public void stopScheduledTests() {
        scheduler.shutdown();
    }

 }





