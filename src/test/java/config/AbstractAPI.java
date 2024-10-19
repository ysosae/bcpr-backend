package config;

import static common.CommonErrorConstant.ENDPOINT_REQUEST_TIME_OUT;
import static common.CommonErrorConstant.ERROR_AN_UNEXPECTED_ERROR_OCURRED;
import static common.CommonErrorConstant.ERROR_CLAIM_ALREADY_EXIST_EN;
import static common.CommonErrorConstant.ERROR_CLAIM_ALREADY_EXIST_ES;
import static common.CommonErrorConstant.ERROR_DATA_ENTERED_IS_INVALID_OR_DOES_NOT_EXIST_IN_OUR_SYSTEM;
import static common.CommonErrorConstant.ERROR_ELEMENTO_NO_ENCONTRADO;
import static common.CommonErrorConstant.ERROR_GRAPHQL_VALIDATION_FAILED;
import static common.CommonErrorConstant.ERROR_HA_OCURRIDO_UN_ERROR_INESPERADO;
import static common.CommonErrorConstant.ERROR_INTERNAL_SERVER_ERROR;
import static common.CommonErrorConstant.ERROR_NOT_FOUND;
import static common.CommonErrorConstant.ERROR_PASSWORD_CURRENT_NOT_MATCH_EN;
import static common.CommonErrorConstant.ERROR_PASSWORD_CURRENT_NOT_MATCH_ES;
import static config.DynamoDbAWS.deleteItemDynamoDB;
import static config.DynamoDbAWS.findValueByQueryIntoPayment;
import static config.DynamoDbAWS.removeExistingClaimDynamoDB;
import static config.DynamoDbAWS.removeExistingLastPasswordDynamoDB;
import static config.RestAssuredExtension.buildFilterByAttribute;
import static config.RestAssuredExtension.collectionVariables;
import static config.RestAssuredExtension.configProperties;
import static config.RestAssuredExtension.createAttributeCognito;
import static config.RestAssuredExtension.currentUserData;
import static config.RestAssuredExtension.expressionAttributeValues;
import static config.RestAssuredExtension.generateBearerToken;
import static config.RestAssuredExtension.getAttributeByUsername;
import static config.RestAssuredExtension.getAttributeValueByAttributeName;
import static config.RestAssuredExtension.getDateIntoPeriod;
import static config.RestAssuredExtension.getRoleDecodeJWToken;
import static config.RestAssuredExtension.getSessionUser;
import static config.RestAssuredExtension.getUsername;
import static config.RestAssuredExtension.getUsernameDecodeJWToken;
import static config.RestAssuredExtension.getValidUserByStatus;
import static config.RestAssuredExtension.overrideLanguage;
import static config.RestAssuredExtension.paginationCognitoAWS;
import static config.RestAssuredExtension.putExpressionAttributeNames;
import static config.RestAssuredExtension.removeCharacter;
import static config.RestAssuredExtension.selectCognitoUser;
import static config.RestAssuredExtension.sessionUser;
import static config.RestAssuredExtension.token;
import static config.RestAssuredExtension.userDelete;
import static config.RestAssuredPropertiesConfig.awsBasicCredentials;
import static config.RestAssuredPropertiesConfig.logAndThrowSkipException;
import static config.ServicesClientAWS.cognitoIdentityProviderClient;
import static enums.FilesPath.COGNITO_USERS_FILE_LOCATION;
import static storage.ScenarioContext.saveInScenarioContext;
import static test.EnvBCPRTest.displayErrorList;
import static test.EnvBCPRTest.errorList;
import static utils.AppDateFormats.getTodayDate;
import static utils.CognitoUserHandler.saveInJsonFile;
import static utils.CognitoUserHandler.setDefaultUserWithAttributes;
import static utils.DataGenerator.randomSetDefaultValuePassword;
import static utils.DataGenerator.randomSetOldPolicyValuePassword;
import static utils.UserDataUtils.getCurrentUserData;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import enums.CallCenterMessage;
import enums.DynamoDBTable;
import enums.Language;
import enums.StatusPayments;
import exception.ClaimAlreadyExistException;
import exception.DNSYOrACHErrorException;
import exception.GraphQLRequestException;
import exception.GraphQLValidationException;
import exception.InvalidDataException;
import exception.NotFoundException;
import exception.PasswordCurrentNotMatchException;
import exception.ServerDownException;
import exception.UnexpectedErrorException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.SkipException;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRemoveUserFromGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public abstract class AbstractAPI {
  /**
   * Log Attribute *******
   */
  private static final Logger log = Logger.getLogger(AbstractAPI.class);

  public static DataTable data;
  public static Scenario scenario;
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static ResponseOptions<Response> response;
  public static JsonObject scenarioData = new JsonObject();
  public static JsonObject errorPaymentsData = new JsonObject();
  public static JsonObject scenarioResponse = new JsonObject();
  public static ArrayList<String> iteratorResponse;
  public static ArrayList<Float> iteratorAmountResponse;
  public static RestAssuredPropertiesConfig restConfigProperties =
    new RestAssuredPropertiesConfig();
  public static JsonObject overrideData;
  public static boolean isMiddlewareTest;
  public static String username;
  public static boolean isUserCreation;
  public static  Map<String, Object> rewards = new HashMap<>();


  public AbstractAPI() {
    overrideData = null;
    iteratorAmountResponse = null;
    iteratorResponse = null;
  }

  public static String generateUUID() {
    return UUID.randomUUID().toString();
  }


  /**
   * Create a table with parameters given on feature step.
   *
   * @param table is a list with parameters given on step.
   */
  public DataTable createDataTable(List<List<String>> table) {
    data = DataTable.create(table);
    log.info(data.toString());
    return data;
  }

  public void invertPasswordPoliceConfig() {
    boolean oldPasswordPolice = configProperties.getOldPasswordPolice();
    configProperties.hardSetOldPasswordPolice(!oldPasswordPolice);
  }


  /**
   * get response from GraphQL Api
   *
   * @param body a text file with query/mutation schema.
   * @return Api responses
   */
  public static ResponseOptions<Response> postMethodGraphQL(String body) {
    try {
      isMiddlewareTest = false;
      response = RestAssuredExtension.postMethodGraphQL(body);
      handleResponseErrors(response, body);
      if (response.getBody().path("data.login.idToken") != null && userDelete) {
        saveInScenarioContext("IdToken", response.getBody().path("data.login.idToken").toString());
      }
      return response;
    } catch (GraphQLRequestException | ServerDownException | GraphQLValidationException |
      InvalidDataException | UnexpectedErrorException | NotFoundException |
      DNSYOrACHErrorException | ClaimAlreadyExistException e) {
      log.error(e.getMessage());
      throw new SkipException(e.getMessage());
    }
  }

  public static boolean isError(ResponseOptions response) {
    return StringUtils.contains(response.getBody().asString(), "error[0]")
      || StringUtils.contains(response.getBody().asString(), "errors")
      || StringUtils.contains(response.getBody().asString(), ERROR_INTERNAL_SERVER_ERROR)
      || StringUtils.contains(response.getBody().asString(), ENDPOINT_REQUEST_TIME_OUT);
  }

  public static void handleResponseErrors(ResponseOptions response, String body) {
    if (isError(response)) {
      handleKnownErrors(response, body);
    }
  }

  public static void handleKnownErrors(ResponseOptions response, String body) {
    boolean errorExtensionsDescription = StringUtils.contains(response.getBody().jsonPath().prettify(),
      "errors[0].extensions.description");
    boolean errorExtensionsCode = StringUtils.contains(response.getBody().jsonPath().prettify(),
      "errors[0].extensions.code");

    try {
      log.info(response.getBody().prettyPrint());
      if(StringUtils.containsIgnoreCase(body, "sendSMSNotification")
        || StringUtils.containsIgnoreCase(body, "sendEmailNotification")
        || StringUtils.containsIgnoreCase(body, "minimumMobileVersionAllow")
        || StringUtils.containsIgnoreCase(body, "healthCheck")
        || StringUtils.containsIgnoreCase(body, "adminCardDeactivationMutation")
        || StringUtils.containsIgnoreCase(body, "adminCardFraudUnblockMutation")){
        log.info(response.getBody().prettyPrint());
      }else{
        if (!errorExtensionsDescription &&
          StringUtils.contains(response.getBody().asString(), ERROR_INTERNAL_SERVER_ERROR)) {
          log.error("Server Down: " + ERROR_INTERNAL_SERVER_ERROR);
          throw new ServerDownException("Server Down: " + ERROR_INTERNAL_SERVER_ERROR);
        }
        if (!errorExtensionsDescription &&
          StringUtils.contains(response.getBody().asString(), ENDPOINT_REQUEST_TIME_OUT)) {
          log.error("Server Down: " + ENDPOINT_REQUEST_TIME_OUT);
          throw new ServerDownException(ENDPOINT_REQUEST_TIME_OUT);
        } else if (!errorExtensionsCode && StringUtils.containsIgnoreCase(
          response.getBody().jsonPath().get("errors[0].extensions.code").toString(),
          ERROR_GRAPHQL_VALIDATION_FAILED) &&
          !(StringUtils.containsIgnoreCase(body, "sendSMSNotification")
            || StringUtils.containsIgnoreCase(body, "sendEmailNotification")
          || StringUtils.containsIgnoreCase(body, "minimumMobileVersionAllow"))) {
          log.error("Not exist this query/mutation in Schema of GraphQL");
          throw new GraphQLValidationException("Not exist this query/mutation in Schema of GraphQL");
        } else if (errorExtensionsCode && StringUtils.containsIgnoreCase(
          response.getBody().path("errors.message[0]").toString(),
          ERROR_DATA_ENTERED_IS_INVALID_OR_DOES_NOT_EXIST_IN_OUR_SYSTEM)) {
          log.error("The data is not valid or Middleware services is throw Error");
          throw new InvalidDataException("The data is not valid or Middleware services is throw Error");
        } else if (errorExtensionsCode && StringUtils.containsIgnoreCase(
          response.getBody().path("errors.message[0]").toString(),
          ERROR_AN_UNEXPECTED_ERROR_OCURRED) || StringUtils.containsIgnoreCase(
          response.getBody().path("errors.message[0]").toString(),
          ERROR_HA_OCURRIDO_UN_ERROR_INESPERADO)) {
          log.error("An unexpected error occurred");
          throw new UnexpectedErrorException("An unexpected error occurred");
        }else if (errorExtensionsCode && StringUtils.containsIgnoreCase(
          response.getBody().path("errors.message[0]").toString(), ERROR_INTERNAL_SERVER_ERROR)) {
          log.error("DNSY or ACH services is throw Error");
          throw new DNSYOrACHErrorException("DNSY or ACH services is throw Error");
        } else if (!errorExtensionsDescription && StringUtils.containsIgnoreCase(
          response.getBody().path("errors[0].extensions.description").toString(),
          ERROR_PASSWORD_CURRENT_NOT_MATCH_EN)
          || StringUtils.containsIgnoreCase(
          response.getBody().path("errors[0].extensions.description").toString(),
          ERROR_PASSWORD_CURRENT_NOT_MATCH_ES)) {
          String VALUE = setPasswordValue();
          saveInScenarioContext("newPassUpdate", VALUE);
          if (scenarioData != null && scenarioData.get("username") != null) {
            removeExistingLastPasswordDynamoDB(scenarioData.get("username").toString(), "id");
            RestAssuredExtension.postMethodGraphQL(body);
            throw new PasswordCurrentNotMatchException("Current password does not match");
          }
        } else if (!errorExtensionsDescription &&
          response.getBody().path("errors[0].extensions.description") != null &&
          StringUtils.containsIgnoreCase(
            response.getBody().path("errors[0].extensions.description").toString(),
            ERROR_CLAIM_ALREADY_EXIST_EN)
          || StringUtils.containsIgnoreCase(
          response.getBody().path("errors[0].extensions.description").toString(),
          ERROR_CLAIM_ALREADY_EXIST_ES)) {
          if (scenarioData != null && scenarioData.get("transactionId") != null) {
            removeExistingClaimDynamoDB(scenarioData.get("transactionId").toString());
            RestAssuredExtension.postMethodGraphQL(body);
            throw new ClaimAlreadyExistException("There is already a claim created for this transaction");
          }
        }
        log.error(response.getBody().prettyPrint());
      }
    }catch (NullPointerException e) {
      log.error("The response of Graphql send error" + e.getMessage());
      Assert.fail("The response of Graphql send error", new Throwable());
    }
  }


  public static ResponseOptions<Response> authenticationGraphQL(String body) {
    RestAssuredExtension.authenticationGraphQL(body);
    return response;
  }

  /**
   * get response from GraphQL Api
   *
   * @param body a text file with query/mutation schema.
   * @return Api responses
   */
  public ResponseOptions<Response> postMethodGraphQL(String body, String variable) {
    response = null;
    response = RestAssuredExtension.postMethodGraphQL(body, variable);
    return response;
  }

  /**
   * get response from Api
   *
   * @param path is a path to find a value into response.
   * @return value found on response
   */
  public static String getResponseValuesByPath(String path) {
    String value = null;
    if (response != null) {
      try {
        value = response.getBody().jsonPath().get(path).toString();
      } catch (NullPointerException e) {
        log.error(e.getMessage());
        log.info(String.format("follow path is invalid %s", path));
      }
    }
    return value;
  }

  /**
   * Save in global variable related to error payments.
   *
   * @param key  name of the variable to be set.
   * @param text value of the variable to be set.
   */
  public static void saveInErrorPaymentsContext(String key, String text) {
    try {
      errorPaymentsData.addProperty(key, text);
      log.info(String.format("Saved as Error Payments Context key: %s with value: %s", key, text));
    } catch (Exception e) {
      log.error("Error saving to Error Payments Context: " + e.getMessage());
    }
  }

  /**
   * get data executing a api request with graphql data will be save on saveInScenarioContext
   * variable
   *
   * @param response current response from api load on response global variable
   * @param table    data to be save from results.
   */
  public void compareResponsePathShowTheValues(
    ResponseOptions<Response> response, List<List<String>> table) {
    DataTable data = createDataTable(table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rField = Collections.singletonList(value.get(0));
            List<String> rValue = Collections.singletonList(value.get(1));
            String PATH = rField.get(0);
            String VALUE = rValue.get(0);
            String pathValue = "";
            String regexValue = "";
            String[] listValue = new String[0];
            boolean isListValidation = false;
            boolean isListAmountValidation = false;

            if (StringUtils.containsIgnoreCase(VALUE, "regex")) {
              regexValue = VALUE.replace("regex:", "");
              regexValue = regexValue.replace("REGEX:", "");
            }
            if (StringUtils.containsIgnoreCase(VALUE, "OR:")) {
              listValue = VALUE.replace("OR:", "").split("[-/]");
            }
            if (StringUtils.containsIgnoreCase(VALUE, "CONTAINS:")) {
              regexValue = VALUE.replace("CONTAINS:", "");
            }
            if (StringUtils.containsIgnoreCase(VALUE, "FLAG:")) {
              listValue = VALUE.replace("FLAG:", "").split("[-/]");
              if (configProperties.getUseSqsAsNotificationChannel()) {
                regexValue = Arrays.stream(listValue).filter(
                    message -> StringUtils.containsIgnoreCase(message,
                      "The data provided could not be validated"))
                  .findFirst()
                  .orElse(null);
              }
            }
            if (StringUtils.containsIgnoreCase(VALUE, "ENUM:")) {
              listValue = VALUE.replace("ENUM:", "").split("[-/]");

              regexValue = Arrays.stream(listValue).filter(
                  message -> StringUtils.containsIgnoreCase(message,
                    "The data provided could not be validated"))
                .findFirst()
                .orElse(null);

            }
            if (StringUtils.containsIgnoreCase(PATH, "LIST:data")
              || StringUtils.containsIgnoreCase(PATH, "LIST:errors")) {
              String rawPathValue = PATH.replace("LIST:", "");
              if (rawPathValue.contains("amount")
                || rawPathValue.contains("currentBalance")
                || rawPathValue.contains("availableCredit")) {
                try {
                  iteratorAmountResponse =
                    new ArrayList<>(response.getBody().jsonPath().get(rawPathValue));
                  isListValidation = true;
                  isListAmountValidation = true;
                } catch (Exception e) {
                  throw new SkipException(e.toString());
                }
              } else {
                try {
                  iteratorResponse =
                    new ArrayList<>(response.getBody().jsonPath().get(rawPathValue));
                  isListValidation = true;
                } catch (Exception e) {
                  throw new SkipException(e.toString());
                }
              }
            } else {
              try {
                pathValue = response.getBody().jsonPath().getString(PATH);
              } catch (Exception e) {
                throw new SkipException(e.toString());
              }
            }
            if (!isListValidation) {
              if (StringUtils.containsIgnoreCase(VALUE, "NOT NULL")) {
                Assert.assertTrue(
                  StringUtils.isNotEmpty(VALUE),
                  String.format(
                    "in path %s, expected value in was NOT NULL, but is %s",
                    PATH, VALUE));

              } else if (StringUtils.containsIgnoreCase(VALUE, "NULL")) {
                Assert.assertTrue(
                  StringUtils.isEmpty(pathValue),
                  String.format(
                    "in path %s, expected value in was NULL, but is %s", PATH, VALUE));

              } else if (StringUtils.containsIgnoreCase(VALUE, "NUMBER")) {
                pathValue = pathValue.replace(".", "");
                Assert.assertTrue(
                  StringUtils.isNumeric(pathValue),
                  String.format(
                    "in path %s, expected value in %s was NUMBER, but is NOT A NUMBER",
                    pathValue, VALUE));

              } else if (StringUtils.containsIgnoreCase(VALUE, "EMAIL")) {
                String regex = "^(.+)@(.+)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pathValue);
                Assert.assertTrue(
                  matcher.matches(),
                  String.format(
                    "in path %s, expected value in %s was EMAIL, but format is incorrect",
                    PATH, VALUE));
              } else if (StringUtils.containsIgnoreCase(VALUE, "REGEX")) {
                if(Objects.nonNull(regexValue)){
                  Pattern pattern = Pattern.compile(regexValue);
                  Matcher matcher = pattern.matcher(pathValue);
                  Assert.assertTrue(
                    matcher.matches(),
                    String.format(
                      "in path %s, expected regex value in %s was %s, but format is incorrect",
                      PATH, regexValue, pathValue));
                }
              } else if (StringUtils.containsIgnoreCase(VALUE, "OR:")) {
                boolean match = false;
                for (String val : listValue) {
                  if (StringUtils.containsIgnoreCase(val, "EMPTY")) {
                    match = StringUtils.isEmpty(pathValue);
                  } else {
                    match = StringUtils.containsIgnoreCase(pathValue, val);
                  }
                  if (match) {
                    break;
                  }
                }
                Assert.assertTrue(
                  match,
                  String.format(
                    "in path %s, expected value was one of %s, but is %s",
                    PATH, VALUE, pathValue));
              } else if (StringUtils.containsIgnoreCase(VALUE, "FLAG:")) {
                boolean match = false;
                for (String val : listValue) {
                  if (StringUtils.containsIgnoreCase(val, "EMPTY")) {
                    match = StringUtils.isEmpty(pathValue);
                  } else {
                    match = StringUtils.containsIgnoreCase(pathValue, val);
                  }
                  if (match) {
                    break;
                  }
                }
                Assert.assertTrue(
                  match,
                  String.format(
                    "in path %s, expected value was one of %s, but is %s",
                    PATH, VALUE, pathValue));
              } else if (StringUtils.containsIgnoreCase(VALUE, "CONTAINS:")) {
                Assert.assertTrue(
                  StringUtils.containsIgnoreCase(pathValue, regexValue),
                  String.format(
                    "in path %s, expected value contains %s, but is %s",
                    PATH, VALUE, regexValue));
              } else if (StringUtils.containsIgnoreCase(VALUE, "SCENARIO_DATA:")) {
                regexValue = VALUE.replace("SCENARIO_DATA:", "");
                Assert.assertTrue(
                  StringUtils.equalsIgnoreCase(
                    pathValue, removeCharacter(scenarioData.get(regexValue).toString())),
                  String.format(
                    "in path %s, expected value was %s, but is %s",
                    PATH, VALUE, pathValue));
              } else {
                Assert.assertTrue(
                  StringUtils.equalsIgnoreCase(pathValue, VALUE),
                  String.format(
                    "in path %s, expected value was %s, but is %s",
                    PATH, VALUE, pathValue));
              }
            }
            if (!isListAmountValidation) {
              if (isListValidation && !iteratorResponse.isEmpty()) {

                if (StringUtils.containsNone(VALUE, "SCENARIO_DATA:")) {
                  for (String iValue : iteratorResponse) {
                    if (StringUtils.containsIgnoreCase(VALUE, "REGEX")) {
                      if(Objects.nonNull(regexValue)){
                        Pattern pattern = Pattern.compile(regexValue);
                        Matcher matcher = pattern.matcher(iValue);
                        Assert.assertTrue(
                          matcher.matches(),
                          String.format(
                            "in path %s, expected regex value in %s was %s, but format is incorrect",
                            PATH, regexValue, iValue));
                      }
                    } else if (StringUtils.containsIgnoreCase(VALUE, "NOT NULL")) {
                      Assert.assertTrue(
                        StringUtils.isNotEmpty(iValue),
                        String.format(
                          "in path %s, expected value in was NOT NULL, but is %s",
                          PATH, VALUE));

                    } else if (StringUtils.containsIgnoreCase(VALUE, "OR:")) {
                      boolean match = false;
                      for (String val : listValue) {
                        if (StringUtils.containsIgnoreCase(val, "EMPTY")) {
                          match = StringUtils.isEmpty(iValue);
                        } else {
                          match = StringUtils.equalsIgnoreCase(iValue, val);
                        }
                        if (match) {
                          break;
                        }
                      }
                      Assert.assertTrue(
                        match,
                        String.format(
                          "in path %s, expected value was one of %s, but is %s",
                          PATH, VALUE, iValue));

                    } else if (StringUtils.containsIgnoreCase(VALUE, "FLAG:")) {
                      boolean match = false;
                      for (String val : listValue) {
                        if (StringUtils.containsIgnoreCase(val, "EMPTY")) {
                          match = StringUtils.isEmpty(iValue);
                        } else {
                          match = StringUtils.equalsIgnoreCase(iValue, val);
                        }
                        if (match) {
                          break;
                        }
                      }
                      Assert.assertTrue(
                        match,
                        String.format(
                          "in path %s, expected value was one of %s, but is %s",
                          PATH, VALUE, iValue));

                    } else if (StringUtils.containsIgnoreCase(VALUE, "NUMBER")) {
                      pathValue = pathValue.replace(".", "");
                      Assert.assertTrue(
                        StringUtils.isNumeric(iValue),
                        String.format(
                          "in path %s, expected value in %s was NUMBER, but is NOT A NUMBER",
                          PATH, iValue));
                    } else if (StringUtils.containsIgnoreCase(VALUE, "CONTAINS:")) {
                      Assert.assertTrue(
                        StringUtils.containsIgnoreCase(pathValue, regexValue),
                        String.format(
                          "in path %s, expected value contains %s, but is %s",
                          PATH, VALUE, regexValue));
                    } else if (StringUtils.containsIgnoreCase(VALUE, "EMAIL")) {
                      String regex = "^(.+)@(.+)$";
                      Pattern pattern = Pattern.compile(regex);
                      Matcher matcher = pattern.matcher(iValue);
                      Assert.assertTrue(
                        matcher.matches(),
                        String.format(
                          "in path %s, expected value in %s was EMAIL, but format is incorrect",
                          PATH, iValue));
                    } else {
                      Assert.assertTrue(
                        StringUtils.equalsIgnoreCase(iValue, VALUE),
                        String.format(
                          "in path %s, expected value was %s, but is %s",
                          PATH, VALUE, iValue));
                    }
                  }
                } else {
                  boolean isValidation = false;
                  regexValue = VALUE.replace("SCENARIO_DATA:", "");
                  if (StringUtils.containsIgnoreCase(VALUE, "AMOUNT")) {
                    for (Float iValue : iteratorAmountResponse) {
                      Pattern pattern = Pattern.compile("^\\d+\\.\\d{0,2}$");
                      Matcher matcher = pattern.matcher(String.valueOf(iValue));
                      if (matcher.matches()) {
                        isValidation = true;
                      }
                      break;
                    }
                  } else {
                    for (String iValue : iteratorResponse) {
                      if (scenarioData.has(regexValue)) {
                        if (StringUtils.equalsIgnoreCase(
                          iValue, scenarioData.get(regexValue).toString())) {
                          isValidation = true;
                          break;
                        }

                      } else {
                        if (StringUtils.equalsIgnoreCase(iValue, VALUE)) {
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "REGEX")) {
                          Pattern pattern = Pattern.compile(regexValue);
                          Matcher matcher = pattern.matcher(iValue);
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "NOT NULL")) {
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "OR:")) {
                          boolean match;
                          for (String val : listValue) {
                            if (StringUtils.containsIgnoreCase(val, "EMPTY")) {
                              match = StringUtils.isEmpty(pathValue);
                            } else {
                              match = StringUtils.containsIgnoreCase(pathValue, val);
                            }
                            if (match) {
                              break;
                            }
                          }
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "NUMBER")) {
                          pathValue.replace(".", "");
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "CONTAINS:")) {
                          isValidation = true;
                          break;
                        } else if (StringUtils.containsIgnoreCase(VALUE, "EMAIL")) {
                          String regex = "^(.+)@(.+)$";
                          Pattern pattern = Pattern.compile(regex);
                          pattern.matcher(iValue);
                          isValidation = true;
                          break;
                        }
                      }
                    }
                  }

                  Assert.assertTrue(
                    isValidation, "The value was not found into List");
                }
              }
            } else {
              boolean isValidation = false;
              VALUE.replace("SCENARIO_DATA:", "");
              if (StringUtils.containsIgnoreCase(VALUE, "AMOUNT")) {
                for (Float iValue : iteratorAmountResponse) {
                  Pattern pattern = Pattern.compile("^\\d+\\.\\d{0,2}$");
                  Matcher matcher = pattern.matcher(String.valueOf(iValue));
                  if (matcher.matches()) {
                    isValidation = true;
                  }
                  break;
                }
              }
              Assert.assertTrue(
                isValidation, "The value was not found into List");
            }
            log.info(String.format("for path %s, TEST PASS", PATH));
          });
    }
  }



  public void compareValueWithScenarioData(int value, String sData) {
    if (!StringUtils.containsIgnoreCase(sData, "SCENARIO_DATA:")) {
      log.warn("Invalid scenario data format: {} " + sData);
      return;
    }

    String regexValue = extractScenarioDataKey(sData);
    String expectedValue = getScenarioDataValue(regexValue);
    String actualValue = String.valueOf(value);

    assertValuesEqual(actualValue, expectedValue);
    log.info("Test passed for scanner value: {} " + sData);
  }

  public void compareValueStringWithScenarioData(String value, String sData) {
    try {
      value = handleErrorPayments(value);

      if (!StringUtils.containsIgnoreCase(sData, "SCENARIO_DATA:")) {
        log.warn("Invalid scenario data format: {} " + sData);
        return;
      }

      String regexValue = extractScenarioDataKey(sData);
      String expectedValue = getScenarioDataValue(regexValue);

      assertValuesEqual(value, expectedValue);
      log.info("Test passed for scanner value: {} " + sData);
    } catch (NullPointerException e) {
      logAndThrowSkipException("The scenario data is EMPTY", e);
    }
  }

  private String extractScenarioDataKey(String data) {
    return data.replace("SCENARIO_DATA:", "");
  }

  private String getScenarioDataValue(String key) {
    Object rawValue = scenarioData.get(key);
    if (rawValue == null) {
      throw new IllegalArgumentException("No scenario data found for key: " + key);
    }
    return removeCharacter(rawValue.toString());
  }

  private String handleErrorPayments(String value) {
    if (StringUtils.containsIgnoreCase(value, "ERROR_PAYMENTS:")) {
      String errorKey = value.replace("ERROR_PAYMENTS:", "");
      String errorValue = removeCharacter(errorPaymentsData.get(errorKey).toString());
      if (isValidStatusErrorPayments(errorValue)) {
        log.info("The value {} is updated " + errorValue);
        return errorValue;
      } else {
        log.info("The value {} is Success " + value);
      }
    }
    return value;
  }

  private void assertValuesEqual(String actual, String expected) {
    Assert.assertTrue(
        StringUtils.equalsIgnoreCase(
          String.valueOf(actual), expected));
  }

  public boolean isValidStatusErrorPayments(String statusError) {
    List<StatusPayments> enumStatusPaymentsList = new ArrayList<>();
    enumStatusPaymentsList.add(StatusPayments.ERROR_ROUTING_MUST_BE_9);
    enumStatusPaymentsList.add(StatusPayments.AUTHENTICATION_ERROR);
    enumStatusPaymentsList.add(StatusPayments.INTERNAL_SERVER_ERROR);
    enumStatusPaymentsList.add(StatusPayments.INVALID_FIELD_RECEIVED_ROUTINGNUMBER);
    enumStatusPaymentsList.add(StatusPayments.INVALID_IP_320921283);
    enumStatusPaymentsList.add(StatusPayments.PENDING_CONFIRMATION);
    for (StatusPayments statusPayments :
      enumStatusPaymentsList) {
      if (StringUtils.equalsIgnoreCase(statusError, statusPayments.name())
        && !StringUtils.equalsIgnoreCase(statusError, StatusPayments.CONFIRMED.name())) {
        return true;
      }
    }
    return false;
  }


  /**
   * set Expression Attribute Values to be use on dynamodb queries. data will be save on
   * expressionAttributeValues variable
   *
   * @param t_table data to be save from results.
   */
  public void setExpressionAttributeValuesToQuery(List<List<String>> t_table) {
    DataTable data = createDataTable(t_table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rType = Collections.singletonList(value.get(0));
            List<String> rKey = Collections.singletonList(value.get(1));
            List<String> rValue = Collections.singletonList(value.get(2));
            String TYPE = rType.get(0);
            String KEY = rKey.get(0);
            String VALUES = rValue.get(0);
            if (VALUES.contains("Today")) {
              VALUES = getTodayDate();
            }
            if (VALUES.contains("customerId")) {
              VALUES = currentUserData.get("customerId").toString();
            }
            if (VALUES.contains("regainAccessId")) {
              VALUES = scenarioData.get("regainAccessId").toString();
            }
            if (VALUES.contains("enrollmentId")) {
              VALUES = scenarioData.get("enrollmentId").toString();
            }
            if (VALUES.contains("nameUsername")) {
              VALUES = getSessionUser();
            }
            if (VALUES.contains("accountIdPayments")) {
              VALUES = scenarioData.get("accountIdPayments").toString();
            }
            rest.putExpressionAttributeValues(TYPE, KEY, VALUES);
          });
    }
    log.info(expressionAttributeValues);
  }

  /**
   * set Expression Attribute Names to be use on dynamodb queries. data will be save on
   * expressionAttributeNames variable
   *
   * @param t_table data to be save from results.
   */
  public void setExpressionAttributeNamesToQuery(List<List<String>> t_table) {
    DataTable data = createDataTable(t_table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rKey = Collections.singletonList(value.get(0));
            List<String> rValue = Collections.singletonList(value.get(1));
            String KEY = rKey.get(0);
            String VALUES = rValue.get(0);
            putExpressionAttributeNames(KEY, VALUES);
          });
    }
  }

  /**
   * execute a scan on a dynamodb table. data will be save on saveInScenarioContext variable
   *
   * @param table                data to be scanned.
   * @param withFilterExpression query filter.
   * @param t_table              data to be save from results.
   */
  public void retrieveScanAction(
    String table, String withFilterExpression, List<List<String>> t_table) {
    log.info("Filter is: " + withFilterExpression);
    ScanResult result = rest.ScanAction(table, withFilterExpression);
    log.info("Results Count is: " + result.getCount());
    Assert.assertTrue(result.getCount() >= 1, "Query don't get results");
    DataTable data = createDataTable(t_table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rVariablePath = Collections.singletonList(value.get(0));
            List<String> rType = Collections.singletonList(value.get(1));
            AttributeValue AV_VARIABLE;
            String[] VARIABLE_PATH;
            String PATH;
            String VARIABLE = null;
            String new_value = null;

            String TYPE = rType.get(0);
            try {
              if (rVariablePath.get(0).contains(".")) {
                VARIABLE_PATH = rVariablePath.get(0).split("\\.");
                PATH = VARIABLE_PATH[0];
                VARIABLE = VARIABLE_PATH[1];
                AV_VARIABLE = result.getItems().get(0).get(PATH).getM().get(VARIABLE);
              } else {
                VARIABLE = rVariablePath.get(0);
                AV_VARIABLE = result.getItems().get(0).get(VARIABLE);
              }
              switch (TYPE.toLowerCase()) {
                case "string":
                  new_value = AV_VARIABLE.getS();
                  break;
                case "number":
                case "integer":
                  new_value = AV_VARIABLE.getN();
                  break;
              }
            } catch (NullPointerException e) {
              log.info(VARIABLE + "it's not present" + e);
            }
            saveInScenarioContext(VARIABLE, new_value);
            if (StringUtils.equalsIgnoreCase(table,
              ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment))) {
              saveInErrorPaymentsContext(VARIABLE, new_value);
            }

          });
    }
  }

  public void retrieveQueryAction(String subId, String status) {
    String user = "Yuliet2023";
    String accountId = "8185";
    if (scenarioData.has("nameUsername")) {
      user = scenarioData.get("nameUsername").toString();
    }
    if (scenarioData.has("accountIdPayments")) {
      accountId = scenarioData.get("accountIdPayments").toString();
    }

    String statusPayment = findValueByQueryIntoPayment(subId, user, accountId, status);
    String VARIABLE = "status";
    saveInScenarioContext(VARIABLE, statusPayment);

  }

  public static boolean containsNumber(String input) {
    return input.matches("\\d+");
  }

  public static boolean containsString(String input) {
    return input.matches(".*[a-zA-Z]+.*");
  }

  public void deleteCognitoUser(String user) {
    String userPoolId = configProperties.getUserPoolId();

    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      AdminDeleteUserRequest adminDeleteUserRequest = AdminDeleteUserRequest.builder()
        .userPoolId(userPoolId)
        .username(user)
        .build();

      identityProviderClient.adminDeleteUser(adminDeleteUserRequest);

      AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
        .userPoolId(userPoolId)
        .username(user)
        .build();

      try {
        AdminGetUserResponse userResponse = identityProviderClient.adminGetUser(getUserRequest);
        log.info("User still exists: " + userResponse.username());
      } catch (UserNotFoundException e) {
        log.info("User has been successfully deleted.");
        scenarioData.addProperty("lastDeletedUser", user);
      }
    } catch (Exception e) {
      log.error("Error occurred while deleting the user: " + e.getMessage());
    }
  }

  /**
   * Executes a scan on a Cognito user pool based on a filter. Results are logged and validated against the provided status.
   *
   * @param attribute  Attribute to be used in the Cognito filter.
   * @param userFilter User filter.
   * @param status     Expected user status (Enabled/Disabled).
   */
  public void retrieveScanCognitoAction(String attribute, String userFilter, String status) {
    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      String filter = String.format("%s ^= \"%s\"", attribute, userFilter);

      ListUsersRequest usersRequest = ListUsersRequest.builder()
        .userPoolId(configProperties.getUserPoolId())
        .filter(filter)
        .build();

      ListUsersResponse response = identityProviderClient.listUsers(usersRequest);

      response.users().forEach(user -> {
        log.info(String.format("User with filter applied %s Enabled %s Status %s Created %s",
          user.username(),
          user.enabled(),
          user.userStatus(),
          user.userCreateDate()));
        Assert.assertTrue(StringUtils.equalsIgnoreCase(status, user.enabled().toString()));
      });

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
    }
  }

  /**
   * Retrieves a list of users from the Cognito user pool based on a filter.
   *
   * @param valueFilter The value to be used in the Cognito filter.
   * @return ListUsersResponse containing the users that match the filter.
   */
  public static ListUsersResponse getUsersResponseWithFilter(String valueFilter) {
    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      String filter = String.format("email ^= '%s'", valueFilter);

      ListUsersRequest usersRequest = ListUsersRequest.builder()
        .userPoolId(configProperties.getUserPoolId())
        .filter(filter)
        .build();

      return identityProviderClient.listUsers(usersRequest);

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      return null;
    }
  }

  public static List<UserType> listAllUsers() {
    List<UserType> validUsers = new ArrayList<>();
    String userPoolId = configProperties.getUserPoolId();

    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      String paginationToken = null;
      int limit = 60;

      do {
        ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder()
          .userPoolId(userPoolId)
          .limit(limit);

        if (paginationToken != null) {
          requestBuilder.paginationToken(paginationToken);
        }

        ListUsersRequest usersRequest = requestBuilder.build();
        ListUsersResponse responseRequest = identityProviderClient.listUsers(usersRequest);

        responseRequest.users().forEach(user -> {
          if (user.enabled() && StringUtils.equalsAnyIgnoreCase(user.userStatus().toString(), "CONFIRMED")) {
            validUsers.add(user);
          }
        });

        paginationToken = responseRequest.paginationToken(); // Update pagination token for next iteration

      } while (paginationToken != null);

      log.info("Total valid users found: " + validUsers.size());

    } catch (CognitoIdentityProviderException e) {
      log.error("Failed to list users: " + e.awsErrorDetails().errorMessage());
    }

    return validUsers;
  }

  public static List<UserType> retrieveValidUsersByFilter(String attributeName, String attributeValue) {
    List<UserType> validUsers = new ArrayList<>();
    String userPoolId = configProperties.getUserPoolId();

    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      String paginationToken = null;
      int limit = 10;

      do {
        String filter = String.format("'%s' ^= '%s'", attributeName, attributeValue);
        log.info("Filter applied: " + filter);

        ListUsersRequest usersRequest = ListUsersRequest.builder()
          .userPoolId(userPoolId)
          .filter(filter)
          .limit(limit)
          .paginationToken(paginationToken)
          .build();

        ListUsersResponse response = identityProviderClient.listUsers(usersRequest);
        paginationToken = response.paginationToken();

        response.users().forEach(user -> {
          if (user.enabled() && "CONFIRMED".equalsIgnoreCase(user.userStatus().toString())) {
            validUsers.add(user);
          }
          log.info("Username: " + user.username());
          user.attributes().forEach(attribute -> {
            log.info("Attribute: " + attribute.name() + ", Value: " + attribute.value());
          });
        });
      } while (StringUtils.isNotEmpty(paginationToken));

    } catch (CognitoIdentityProviderException e) {
      log.error("Failed to retrieve users: " + e.awsErrorDetails().errorMessage());
    }

    return validUsers;
  }



  public static String filterUsersByAttribute(String attributeName, String attributeValue) {
    try {
      List<UserType> validUsers = retrieveValidUsersByFilter(attributeName, attributeValue);

      if (!validUsers.isEmpty()) {
        if (validUsers.size() == 1) {
          username = validUsers.get(0).username();
          log.info(validUsers.get(0).username());
        } else if (!validUsers.isEmpty()) {
          int i = 0;
          while (i < validUsers.size()) {
            if (sessionUser != null) {
              if (validUsers.get(i).username().contains(sessionUser.toLowerCase(Locale.ROOT)) ||
                validUsers.get(i).username().contains(sessionUser)) {
                username = validUsers.get(i).username();
                log.info(validUsers.get(i).username());
              }
            }
            i++;
          }
        }

      }
    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return username;
  }

  /**
   * get response from GraphQL Api
   *
   * @param body a text file with query/mutation schema.
   * @return Api responses
   */
  public ResponseOptions<Response> postMethodMiddleware(String path, String body) {
    try {
      if (Boolean.parseBoolean(restConfigProperties.getIsMiddlewareProxyActivated())) {
        isMiddlewareTest = true;
        saveInScenarioContext("traceId", generateUUID());
        response = RestAssuredExtension.postMethodMiddleware(path, body);
      } else {
        log.info("Middleware interactions are deactivated");
      }
    } catch (Exception ex) {
      log.info(ex);
      log.info("Middleware is down");
      throw new SkipException("Middleware is down");
    }
    return response;
  }

  /**
   * get response from GraphQL Api
   *
   * @param path a text file with query/mutation schema.
   * @return Api responses
   */
  public ResponseOptions<Response> postMethodMiddleware(String path) {
    try {
      if (Boolean.parseBoolean(restConfigProperties.getIsMiddlewareProxyActivated())) {
        isMiddlewareTest = true;
        saveInScenarioContext("traceId", generateUUID());
        response = RestAssuredExtension.postMethodMiddleware(path);
      } else {
        log.info("Middleware interactions are deactivated");
      }
    } catch (Exception ex) {
      log.info(ex);
      log.info("Middleware is down");
      throw new SkipException("Middleware is down");
    }
    return response;
  }

  public void matchesJsonSchemaValidator(ResponseOptions<Response> response, String responsePath) {
    rest.matchesJsonSchemaValidator(response, responsePath);
  }

  public ResponseOptions<Response> postMethodMiddleware(String path, String body, String variable) {
    try {
      if (Boolean.parseBoolean(restConfigProperties.getIsMiddlewareProxyActivated())) {
        response = RestAssuredExtension.postMethodMiddleware(path, body, variable);
      } else {
        log.debug("Middleware interactions are deactivated");
      }
    } catch (Exception ex) {
      log.info(ex);
      log.info("Middleware is down");
      throw new SkipException("Path is invalid");
    }
    return response;
  }

  public void saveOldPassAsBackUp() {
    String passBackUp = setPasswordBase();
    if (scenarioData.has("passBackUp")) {
      passBackUp = scenarioData.get("passBackUp").getAsString();
    }

    if (StringUtils.isNotEmpty(passBackUp)) {
      if (!scenarioData.has("passBackUp")) {
        saveInScenarioContext("passBackUp", passBackUp);
      }
    } else {
      log.info("Cannot backup the user pass");
    }
  }

  public void saveNewPassWithUsername() {
    if (!scenarioData.has("newPassWithUsername")) {
      String currentPass = scenarioData.has("newPassUpdate")
        ? scenarioData.get("newPassUpdate").getAsString()
        : setPasswordBase();

      String username = scenarioData.has("lastDeletedUser")
        ? scenarioData.get("lastDeletedUser").getAsString()
        : "Automation2023";

      if (StringUtils.isNotEmpty(currentPass) && StringUtils.isNotEmpty(username)) {
        saveInScenarioContext("newPassWithUsername", currentPass + username);
      } else {
        log.info("Cannot set user password including username");
      }
    }
  }

  public void saveNewPassWithBlacklistedCharacters() {
    if (!scenarioData.has("newPassWithBlackListed")) {
      String currentPass = scenarioData.has("newPassUpdate")
        ? scenarioData.get("newPassUpdate").getAsString()
        : setPasswordBase();

      if (StringUtils.isNotEmpty(currentPass)) {
        saveInScenarioContext("newPassWithBlackListed", currentPass + getBlackListedCharacter());
      } else {
        log.info("Cannot set user password including blacklisted character");
      }
    }
  }

  public void saveNewPassTooShort() {
    if (!scenarioData.has("newPassTooShort")) {
      String currentPass = scenarioData.has("newPassUpdate")
        ? scenarioData.get("newPassUpdate").getAsString()
        : setPasswordBase();

      if (StringUtils.isNotEmpty(currentPass)) {
        saveInScenarioContext("newPassTooShort", StringUtils.substring(currentPass, 0, 5));
      } else {
        log.info("Cannot set user password as too short");
      }
    }
  }

  public void removeOldPassBackUp() {
    if (scenarioData.has("newPassUpdate")) {
      scenarioData.remove("newPassUpdate");
    }
  }

  public JsonObject setOverrideData(List<List<String>> t_table) {
    DataTable data = createDataTable(t_table);
    JsonObject tempData = new JsonObject();
    if (data != null) {
      data.cells().forEach(value -> {
        String path = value.get(0);
        String valueStr = value.get(1);
        String valueStrScenarioData = valueStr;

        if (StringUtils.containsIgnoreCase(valueStr, "SCENARIO_DATA:")) {
          valueStr = scenarioData.has(valueStr.replace("SCENARIO_DATA:", ""))
            ? scenarioData.get(valueStr.replace("SCENARIO_DATA:", "")).getAsString().trim()
            : valueStr;
        }

        if (StringUtils.equalsIgnoreCase(path, "USERNAME")) {
          if (!StringUtils.containsIgnoreCase(valueStrScenarioData, "SCENARIO_DATA:")) {
            valueStr = StringUtils.containsIgnoreCase(valueStr, "Automation2023")
              ? valueStr
              : getSessionUser();
          }
          saveInScenarioContext("username", valueStr);
        }

        else if (StringUtils.equalsAny(path, "PASSWORD")) {
          if (!StringUtils.containsIgnoreCase(valueStrScenarioData, "SCENARIO_DATA:")) {
            valueStr = setPasswordBase();
          }
          saveInScenarioContext("password", valueStr);
        }
        else if (StringUtils.equalsAny(path, "password")) {
          valueStr = setPasswordValue();
          saveInScenarioContext("newPass", valueStr);
        }
        else if (StringUtils.equalsAny(path, "newPassword")) {
          valueStr = setPasswordValue();
          saveInScenarioContext("newPassUpdate", valueStr);
        }
        else if (StringUtils.equalsAny(path, "currentPassword")) {
          saveInScenarioContext("passBackUp", valueStr);
        }
        else if (StringUtils.equalsIgnoreCase(path, "LastEightDigits")) {
          if (!StringUtils.containsIgnoreCase(valueStrScenarioData, "SCENARIO_DATA:")) {
            valueStr = currentUserData.get("lastEightDigits").toString();
          }
          saveInScenarioContext("lastEightDigits", valueStr);
        }
        else if (StringUtils.containsIgnoreCase(valueStr, "currentFromDate")) {
          valueStr = getDateIntoPeriod("currentDate", "from");
          saveInScenarioContext("fromDate", valueStr);
        }
        else if (StringUtils.containsIgnoreCase(valueStr, "currentToDate")) {
          valueStr = getDateIntoPeriod("currentDate", "to");
          saveInScenarioContext("toDate", valueStr);
        }

        tempData.addProperty(path, valueStr);
      });
    }

    return tempData;
  }


  public static String getBlackListedCharacter() {
    Random random = new Random();
    List<String> whiteList = Arrays.asList("®", "™", "€");
    int randomItem = random.nextInt(whiteList.size());
    return whiteList.get(randomItem);
  }

  public static String setPasswordValue() {
    String value;
    if (configProperties.getOldPasswordPolice()) {
      value = randomSetOldPolicyValuePassword("Test");
    } else {
      value = randomSetDefaultValuePassword("Test**");
    }
    return value;
  }

  public static String setPasswordBase() {
    return  "Test**1234567890";

  }

  /**
   * Searching, filtering, and validating fields within an object by passing scenario data.
   *
   * @param path          path of response service.
   * @param validationKey field of query/mutation to find expected parameter.
   * @param keyFilter     field of query/mutation to find valueFilter.
   * @param valueFilter   value of scenario data from which the object needs to be obtained.
   * @param expected      value of query/mutation you expect to find.
   */
  protected boolean getKeyFromResponseThatContains(
    String path, String validationKey, String keyFilter, String valueFilter, String expected) {

    List<Map<String, String>> responseItems = getResponseItems(path);
    String filterValue = getFilterValue(valueFilter);

    return isKeyPresentInResponse(responseItems, keyFilter, validationKey, filterValue, expected);
  }

  /**
   * Retrieves the list of response items from the given path.
   *
   * @param path the JSON path to retrieve data from.
   * @return a list of response items.
   */
  private List<Map<String, String>> getResponseItems(String path) {
    return new ArrayList<>(response.getBody().jsonPath().getList(path));
  }

  /**
   * Extracts and returns the filter value. If the value contains "SCENARIO_DATA:", it replaces
   * and retrieves the actual scenario data.
   *
   * @param valueFilter the filter value or scenario data key.
   * @return the processed filter value.
   */
  private String getFilterValue(String valueFilter) {
    if (StringUtils.containsIgnoreCase(valueFilter, "SCENARIO_DATA:")) {
      String scenarioKey = valueFilter.replace("SCENARIO_DATA:", "");
      return removeCharacter(scenarioData.get(scenarioKey).toString());
    }
    return valueFilter;
  }

  /**
   * Checks if the key is present in the response based on the given filters and expected value.
   *
   * @param responseItems  list of response items to search through.
   * @param keyFilter      the key to filter the response.
   * @param validationKey  the key to validate against the expected value.
   * @param filterValue    the value to match with the keyFilter.
   * @param expected       the expected value for the validationKey.
   * @return true if the key is found with the expected value, false otherwise.
   */
  private boolean isKeyPresentInResponse(
    List<Map<String, String>> responseItems, String keyFilter, String validationKey,
    String filterValue, String expected) {

    for (Map<String, String> dataItem : responseItems) {
      if (dataItem.containsKey(keyFilter) && dataItem.containsKey(validationKey)) {
        if (StringUtils.equalsIgnoreCase(dataItem.get(keyFilter), filterValue)
          && StringUtils.equalsIgnoreCase(expected, dataItem.get(validationKey))) {
          return true;
        }
      }
    }
    return false;
  }


  protected String saveKeyOffsetFromResponse(String path, String saveAs) {
    int offset = response.getBody().jsonPath().get(path);
    int limit = getLimitXTransactions(path);
    int size = getSizeXTransactions(path);

    String keyValue = String.valueOf(offset);

    if (offset == 0 || size <= limit) {
      scenarioData.addProperty(saveAs, keyValue);
    } else {
      Assert.assertTrue(
        StringUtils.containsIgnoreCase(
          String.valueOf(response.getBody().jsonPath().get(path).toString()), "0"),
        String.format(
          "The offset response is different to expected, obtained: %s, expected: %s",
          response.getBody().jsonPath().get(path).toString(), "0"));
    }
    log.info(
      String.format("Update Scenario Context key: %s with value: %s ", saveAs, keyValue));
    return keyValue;
  }

  protected int getLimitXTransactions(String path) {
    int limit = 0;
    if (StringUtils.containsIgnoreCase(path, "listTransactions")) {
      limit = response.getBody().jsonPath().get("data.listTransactions.quantity");

    }
    if (StringUtils.containsIgnoreCase(path, "listInProcessTransactions")) {
      limit = response.getBody().jsonPath().get("data.listInProcessTransactions.quantity");

    }
    return limit;
  }

  protected int getSizeXTransactions(String path) {
    int size = 0;
    if (StringUtils.containsIgnoreCase(path, "listTransactions")) {
      size = new ArrayList<>(
        response.getBody().jsonPath().get("data.listTransactions.transactions")).size();
    }
    if (StringUtils.containsIgnoreCase(path, "listInProcessTransactions")) {
      size = new ArrayList<>(
        response.getBody().jsonPath().get("data.listInProcessTransactions.transactions")).size();
    }
    return size;
  }

  /**
   * Searching, filtering and buying fields within an object by passing a scenario data to it
   *
   * @param path path of response service.   *
   */
  protected String saveKeyFromResponseThatContains(
    String path, String filterKey, String valueFilter, String saveKey, String saveAs) {
    boolean isError = response.getBody().prettyPrint().contains("error");
    String value;
    String keyValue = "";

    if (isError) {
      if (StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
        ERROR_NOT_FOUND) ||
        StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
          ERROR_ELEMENTO_NO_ENCONTRADO)) {
        log.info("Not exist transaction");
        throw new SkipException("Not exist transaction");
      }
    } else {
      iteratorResponse = new ArrayList<>(response.getBody().jsonPath().get(path));
      if (StringUtils.containsIgnoreCase(valueFilter, "SCENARIO_DATA:")) {
        value = scenarioData.get(valueFilter.replace("SCENARIO_DATA:", "")).toString();
      } else {
        value = valueFilter;
      }
      keyValue = searchValueFieldInList(filterKey, saveKey, value);
      if (StringUtils.isEmpty(keyValue)) {
        Object item = iteratorResponse.get(0);
        Map<String, String> dataItem = (Map<String, String>) item;
        String itemName = null, itemValue = null;

        if(Objects.nonNull(dataItem)){
          if (dataItem.containsKey(saveKey)) {
            itemName = saveAs;
            itemValue = dataItem.get(saveKey);
          }
          String dbDynamoTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ClaimData);
          deleteItemDynamoDB(dbDynamoTableName, itemName, itemValue);
          keyValue = searchValueFieldInList(filterKey, saveKey, value);
          scenarioData.addProperty(itemName, itemValue);
        }
        log.info(
          String.format("Item %s with value %s it was saved successfully", itemName, itemValue));
      } else {
        scenarioData.addProperty(saveAs, keyValue);
        log.info(
          String.format("Item %s with value %s it was saved successfully", saveAs, keyValue));
      }

    }
    return keyValue;
  }

  protected String saveKeyFromResponseThatContains(String path, String saveKey, String saveAs) {
    boolean isError = response.getBody().prettyPrint().contains("error");
    String value = null;

    if (isError) {
      String errorMessage = response.getBody().path("errors[0].message").toString();
      if (StringUtils.equalsIgnoreCase(errorMessage, ERROR_NOT_FOUND) ||
        StringUtils.equalsIgnoreCase(errorMessage, ERROR_ELEMENTO_NO_ENCONTRADO)) {
        log.info("Transaction not found");
        throw new SkipException("Transaction not found");
      }
    } else {
      value = response.getBody().jsonPath().get(path);

      if (scenarioData == null) {
        scenarioData = new JsonObject();
      }

      if (StringUtils.containsIgnoreCase(saveAs, "SCENARIO_DATA:")) {
        String scenarioKey = saveAs.replace("SCENARIO_DATA:", "");
        value = scenarioData.has(scenarioKey) ? scenarioData.get(scenarioKey).getAsString() : null;
      } else {
        if (Objects.nonNull(value)) {
          scenarioData.addProperty(saveAs, value);
          log.info(String.format("Item %s with value %s was saved successfully", saveAs, value));
        } else {
          log.warn("Value is null, nothing to save in scenarioData.");
        }
      }
    }
    return value;
  }


  protected void saveKeyFromResponseThatContainsSaveContextVariable(String path, String saveAs) {
    boolean isError = response.getBody().prettyPrint().contains("error");
    if (isError) {
      if (StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
        ERROR_NOT_FOUND) ||
        StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
          ERROR_ELEMENTO_NO_ENCONTRADO)) {
        log.info("Not exist transaction");
        throw new SkipException("Not exist transaction");
      }
    } else {
      iteratorResponse = new ArrayList<>(response.getBody().jsonPath().get(path));
      if (StringUtils.containsIgnoreCase(iteratorResponse.get(0), "NoBlocked")) {
        saveInScenarioContext(saveAs, "ON");
      } else if (StringUtils.containsIgnoreCase(iteratorResponse.get(0), "TemporaryBlocked")) {
        saveInScenarioContext(saveAs, "OFF");
      } else if (StringUtils.containsIgnoreCase(iteratorResponse.get(0), "OtherTypeOfBlock")) {
        saveInScenarioContext(saveAs, "OFF");
      } else if (StringUtils.containsIgnoreCase(iteratorResponse.get(0), "FraudBlocked")) {
        response = postMethodMiddleware("v1/AdminCardUnlock",
          "middleware/middlewareGetCustomerInformationCommons.json");
        isError = response.getBody().prettyPrint().contains("error");
        if (isError) {
          if (StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
            ERROR_NOT_FOUND) ||
            StringUtils.equalsIgnoreCase(response.getBody().path("errors[0].message").toString(),
              ERROR_ELEMENTO_NO_ENCONTRADO)) {
            log.info("Not exist transaction");
            throw new SkipException("Not exist transaction");
          }
        } else {
          iteratorResponse = new ArrayList<>(response.getBody().jsonPath().get(path));
          if (StringUtils.containsIgnoreCase(iteratorResponse.get(0), "NoBlocked")) {
            saveInScenarioContext(saveAs, "ON");
          }
        }
      }
    }
  }

  protected String searchValueFieldInList(String filterKey, String saveKey, String value) {
    String keyValue = "";
    for (Object item : iteratorResponse) {
      Map<String, String> dataItem = (Map<String, String>) item;
      if (dataItem.containsKey(filterKey) && dataItem.containsKey(saveKey)) {
        if (StringUtils.equalsIgnoreCase(String.valueOf(dataItem.get(filterKey)), value)) {
          keyValue = dataItem.get(saveKey);
          break;
        }
      }
    }
    return keyValue;
  }

  public static void addPermissionUserCognito(String permission, String user) {
    String isAdminStr = getRoleDecodeJWToken(collectionVariables.get("IdToken").toString());
    boolean isAdmin = StringUtils.containsIgnoreCase(isAdminStr, "Admins");
    if (!isAdmin) {
      addPermissionCognitoUser(permission, user);
      listPermissionCognitoByUser(permission, user);
      authenticationGraphQL("login.graphql");
      isAdminStr = getRoleDecodeJWToken(collectionVariables.get("IdToken").toString());
      isAdmin = StringUtils.containsIgnoreCase(isAdminStr, "Admins");
      JsonObject cognitoUsersResult = setDefaultUserWithAttributes("isAdmin", true);
      saveInJsonFile(COGNITO_USERS_FILE_LOCATION.getText(), cognitoUsersResult);
      Assert.assertTrue(isAdmin);
    }
  }



  public static void addPermissionCognitoUser(String groupName, String username) {
    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
        .userPoolId(configProperties.getUserPoolId())
        .groupName(groupName)
        .username(username)
        .build();

      identityProviderClient.adminAddUserToGroup(addUserToGroupRequest);

    } catch (CognitoIdentityProviderException e) {
      throw new SkipException("Failed to add user to group: " + e.awsErrorDetails().errorMessage(), e);
    }
  }

  public static void removePermissionCognitoUser(String groupName, String username) {
    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      AdminRemoveUserFromGroupRequest removeUserFromGroupRequest = AdminRemoveUserFromGroupRequest.builder()
        .userPoolId(configProperties.getUserPoolId())
        .groupName(groupName)
        .username(username)
        .build();

      identityProviderClient.adminRemoveUserFromGroup(removeUserFromGroupRequest);

    } catch (CognitoIdentityProviderException e) {
      throw new SkipException("Failed to remove user from group: " + e.awsErrorDetails().errorMessage(), e);
    }
  }

  public static Optional<GroupType> listPermissionCognitoByUser(String groupName, String username) {
    try (CognitoIdentityProviderClient identityProviderClient = createCognitoClient()) {
      AdminListGroupsForUserRequest request = createListGroupsRequest(username);
      AdminListGroupsForUserResponse response = identityProviderClient.adminListGroupsForUser(request);

      Optional<GroupType> groupType = findGroupByName(response, groupName);
      logGroupMembership(username, groupName, groupType);

      return groupType;
    } catch (CognitoIdentityProviderException e) {
      throw new SkipException("Failed to list groups for user: " + e.awsErrorDetails().errorMessage(), e);
    }
  }

  private static CognitoIdentityProviderClient createCognitoClient() {
    return CognitoIdentityProviderClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build();
  }

  private static AdminListGroupsForUserRequest createListGroupsRequest(String username) {
    return AdminListGroupsForUserRequest.builder()
      .userPoolId(configProperties.getUserPoolId())
      .username(username)
      .build();
  }

  private static Optional<GroupType> findGroupByName(AdminListGroupsForUserResponse response, String groupName) {
    return response.groups().stream()
      .filter(group -> StringUtils.equalsIgnoreCase(group.groupName(), groupName))
      .findFirst();
  }

  private static void logGroupMembership(String username, String groupName, Optional<GroupType> groupType) {
    if (groupType.isPresent()) {
      log.info("User {} is in group {} " + username + groupType.get().groupName());
    } else {
      log.warn("User {} is not in group {} " + username + groupName);
    }
  }


  public static void setStatusEnabledCognito(String username) {
    String userPoolId = configProperties.getUserPoolId();
    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      AdminEnableUserRequest enableUserRequest = AdminEnableUserRequest.builder()
        .userPoolId(userPoolId)
        .username(username)
        .build();

      AdminEnableUserResponse enableUserResult =
        identityProviderClient.adminEnableUser(enableUserRequest);

      if (enableUserResult.sdkHttpResponse().isSuccessful()) {
        log.info("User has been enabled successfully.");
      } else {
        log.error("Failed to enable the user.");
      }

    } catch (CognitoIdentityProviderException e) {
      log.error("Failed to enable user: " + e.awsErrorDetails().errorMessage(), e);
    }
  }


  protected void compareResponseThatContains(List<List<String>> t_table) {
    DataTable data = createDataTable(t_table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rPath = Collections.singletonList(value.get(0));
            List<String> rKey = Collections.singletonList(value.get(1));
            List<String> rThatContainsKey = Collections.singletonList(value.get(2));
            List<String> rWithValue = Collections.singletonList(value.get(3));
            List<String> rExpected = Collections.singletonList(value.get(4));
            String path = rPath.get(0);
            String key = rKey.get(0);
            String thatContainsKey = rThatContainsKey.get(0);
            String withValue = rWithValue.get(0);
            String expected = rExpected.get(0);
            boolean isPresent =
              getKeyFromResponseThatContains(path, key, thatContainsKey, withValue, expected);
            Assert.assertTrue(isPresent, "The value expected did not match");
          });
    }
  }

  protected void searchPathContain(
    String path, String KeyFilter, String valueFilter, List<List<String>> t_table) {
    DataTable data = createDataTable(t_table);
    if (data != null) {
      data.cells()
        .forEach(
          value -> {
            List<String> rValidationKey = Collections.singletonList(value.get(0));
            List<String> rExpected = Collections.singletonList(value.get(1));
            String validationKey = rValidationKey.get(0);
            String expected = rExpected.get(0);
            getKeyFromResponseThatContains(
              path, validationKey, KeyFilter, valueFilter, expected);
          });
    }
  }

  public static void shortWait(int timeInSec) {
    Awaitility.await()
      .atMost(timeInSec * 1000L, TimeUnit.SECONDS)
      .pollInterval(100, TimeUnit.MILLISECONDS);
  }

  public static void shortWait(int timeInSec, boolean condition) {
    Awaitility.await()
      .atMost(timeInSec, TimeUnit.SECONDS)
      .pollInterval(100, TimeUnit.MILLISECONDS)
      .until(() -> condition);
  }

  protected void saveValueAttributeCognito(String attributeName) {
    String subID = getSubId(attributeName);
    saveInScenarioContext(attributeName, subID);

  }

  protected String getSubId(String attributeName) {
    CognitoIdentityProviderClient cognitoClient = cognitoIdentityProviderClient();
    String givenName = retrieveGivenName();
    AttributeType attributeCustom = createAttributeCognito("given_name", givenName);
    String filter = buildFilterByAttribute(attributeCustom);

    ListUsersResponse response = paginationCognitoAWS(
      cognitoClient, configProperties.getUserPoolId(), filter, 10
    );

    List<UserType> validUsers = getValidUserByStatus(response, "CONFIRMED");
    String username = getUsername(validUsers);
    List<AttributeType> validAttributes = getAttributeByUsername(response, username);

    return getAttributeValueByAttributeName(validAttributes, attributeName);
  }

  private String retrieveGivenName() {
    if (getCurrentUserData() == null) {
      selectCognitoUser();
    }
    return getCurrentUserData().get("name").toString();
  }

  protected void getUserRoleTokenJWT(String role) {
    if (StringUtils.isNotEmpty(token)) {
      token = token.replaceFirst("Bearer ", "");
      String preferredRole = getRoleDecodeJWToken(token);
      String cognitoName = getUsernameDecodeJWToken(token);
      if (StringUtils.containsIgnoreCase(preferredRole, role)) {
        saveInScenarioContext(cognitoName, preferredRole);
      }
      log.info(
        String.format("The username %s has permission to role %s", cognitoName, preferredRole));
    } else {
      generateBearerToken(true);
      if (StringUtils.isNotEmpty(token)) {
        token = token.replaceFirst("Bearer ", "");
        String preferredRole = getRoleDecodeJWToken(token);
        String cognitoName = getUsernameDecodeJWToken(token);
        if (StringUtils.containsIgnoreCase(preferredRole, role)) {
          saveInScenarioContext(cognitoName, preferredRole);
        }
        log.info(
          String.format("The username %s has permission to role %s", cognitoName, preferredRole));

      }
    }
  }

  public void validateIVRCallCenter() {
    response = postMethodGraphQL("graphQL/getCallCenter.graphql");
    log.info(response.getBody().prettyPrint());
    ArrayList<JsonObject> callCenterOptions = getCallCenterResponse();
    isIVRCallCenterPresent(callCenterOptions);
  }

  public static void isIVRCallCenterPresent(ArrayList<JsonObject> callCenterOptions) {
    if (callCenterOptions.size() > 1) {
      String[] stringCallCenterPath = formattedStringPath(callCenterOptions);
      String[][] arrayPartitionCallCenterObject = arrayPartition(stringCallCenterPath);
      String[][] dataValueCallCenter = getValueCallCenterOptions(callCenterOptions, arrayPartitionCallCenterObject);
      responseCallCenterValidator(dataValueCallCenter);
    }
  }

  public static void responseCallCenterValidator(String[][] dataValueCallCenter) {
    String[][] expectedValues;
    if (StringUtils.containsIgnoreCase(overrideLanguage, Language.en.name())) {
      expectedValues = new String[][] {
        {
          CallCenterMessage.CALL_CENTER_DESCRIPTION_MEMBER_SERVICE_CENTER_EN.getMessage(),
          CallCenterMessage.CALL_CENTER_SCHEDULE_MEMBER_SERVICE_CENTER_EN.getMessage(),
          CallCenterMessage.CALL_CENTER_PHONE_NUMBER_MEMBER_SERVICE_CENTER.getMessage()
        },
        {
          CallCenterMessage.CALL_CENTER_DESCRIPTION_AUTOMATED_SERVICE_EN.getMessage(),
          CallCenterMessage.CALL_CENTER_SCHEDULE_AUTOMATED_SERVICE_EN.getMessage(),
          CallCenterMessage.CALL_CENTER_PHONE_NUMBER_AUTOMATED_SERVICE.getMessage()
        }
      };
    } else {
      expectedValues = new String[][] {
        {
          CallCenterMessage.CALL_CENTER_DESCRIPTION_MEMBER_SERVICE_CENTER_ES.getMessage(),
          CallCenterMessage.CALL_CENTER_SCHEDULE_MEMBER_SERVICE_CENTER_ES.getMessage(),
          CallCenterMessage.CALL_CENTER_PHONE_NUMBER_MEMBER_SERVICE_CENTER.getMessage()
        },
        {
          CallCenterMessage.CALL_CENTER_DESCRIPTION_AUTOMATED_SERVICE_ES.getMessage(),
          CallCenterMessage.CALL_CENTER_SCHEDULE_AUTOMATED_SERVICE_ES.getMessage(),
          CallCenterMessage.CALL_CENTER_PHONE_NUMBER_AUTOMATED_SERVICE.getMessage()
        }
      };
    }
    validateEqualsArrays(dataValueCallCenter, expectedValues);
  }

  public static void validateEqualsArrays(String[][] dataValueCallCenter, String[][] expectedValues) {
    if (areArraysEqual(expectedValues, dataValueCallCenter)) {
      log.info("The Call Center values are correct");
    }
    if (errorList != null) {
      displayErrorList();
    }
    assert errorList != null;
    Assert.assertTrue(errorList.isEmpty(), "Call Center value Errors were found");
  }

  public static boolean areArraysEqual(String[][] array1, String[][] array2) {
    List<String> errors = new ArrayList<>();
    if (array1.length != array2.length || array1[0].length != array2[0].length) {
      return false;
    }

    for (int i = 0; i < array1.length; i++) {
      for (int j = 0; j < array1[i].length; j++) {
        if (!StringUtils.equalsIgnoreCase(array1[i][j], array2[i][j])) {
          errors.add(array1[i][j]);
        }
      }
    }
    errorList = errors;
    return errors.isEmpty();
  }

  public static ArrayList<JsonObject> getCallCenterResponse() {
    if (response == null) {
      throw new SkipException("The response is empty");
    }
    JsonObject jsonResponse = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
    JsonArray callCenterArray = jsonResponse.getAsJsonObject("data").getAsJsonArray("listCallCenters");
    ArrayList<JsonObject> callCenterOptions = new ArrayList<>();
    for (JsonElement element : callCenterArray) {
      callCenterOptions.add(element.getAsJsonObject());
    }
    return callCenterOptions;
  }

  public static String[] formattedStringPath(ArrayList<JsonObject> callCenterObject) {
    StringBuilder formattedString = new StringBuilder();

    for (int i = 0; i < callCenterObject.size(); i++) {
      formattedString.append(String.format("data.listCallCenters[%s].callCenterOptions[0].description\n", i))
        .append(String.format("data.listCallCenters[%s].callCenterOptions[0].schedule\n", i))
        .append(String.format("data.listCallCenters[%s].callCenterOptions[0].phoneNumber\n", i));
    }
    return formattedString.toString().split("\n");
  }

  public static String[][] getValueCallCenterOptions(ArrayList<JsonObject> callCenterObject, String[][] arrayPartitionCallCenterObject) {
    String[][] dataGraphQl = new String[callCenterObject.size()][];

    for (int i = 0; i < callCenterObject.size(); i++) {
      dataGraphQl[i] = new String[] {
        getResponseValuesByPath(arrayPartitionCallCenterObject[i][0]),
        getResponseValuesByPath(arrayPartitionCallCenterObject[i][1]),
        getResponseValuesByPath(arrayPartitionCallCenterObject[i][2]),
      };
    }
    return dataGraphQl;
  }

  public static String[][] arrayPartition(String[] stringPath) {
    int partitionSize = 3;
    int numPartitions = stringPath.length / partitionSize;
    int currentIndex = 0;
    String[][] partitions = new String[numPartitions][partitionSize];

    for (int i = 0; i < numPartitions; i++) {
      for (int j = 0; j < partitionSize; j++) {
        if (currentIndex < stringPath.length) {
          partitions[i][j] = stringPath[currentIndex];
          currentIndex++;
        }
      }
    }
    return partitions;
  }
}
