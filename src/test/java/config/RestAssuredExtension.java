package config;

import static common.CommonErrorConstant.ERROR_AN_UNEXPECTED_ERROR_OCURRED;
import static common.CommonErrorConstant.ERROR_HA_OCURRIDO_UN_ERROR_INESPERADO;
import static common.CommonErrorConstant.ERROR_NO_PUDIERON_VALIDARSE_LOS_DATOS_INGRESADOS;
import static common.CommonErrorConstant.ERROR_THE_DATA_PROVIDED_IS_INCORRECT;
import static config.AbstractAPI.filterUsersByAttribute;
import static config.AbstractAPI.isUserCreation;
import static config.AbstractAPI.listAllUsers;
import static config.AbstractAPI.scenarioData;
import static config.AbstractAPI.setPasswordBase;
import static config.AbstractAPI.shortWait;
import static config.AbstractAPI.username;
import static config.RestAssuredPropertiesConfig.*;
import static enums.FilesPath.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static storage.ScenarioContext.saveInScenarioContext;
import static utils.AppDateFormats.dateSubtraction;
import static utils.AppDateFormats.getTodayDate;
import static utils.AppDateFormats.yyyyMMddPattern;
import static utils.DataGenerator.randomNumber;
import static utils.DataGenerator.randomOperation;
import static utils.DataGenerator.randomPassword;
import static utils.DataGenerator.randomUsername;
import static utils.UserDataUtils.getCurrentUserData;
import static utils.UserDataUtils.getCurrentUserDataByUsername;
import static utils.UserDataUtils.getUserDataFromFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import io.restassured.specification.RequestSpecification;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.openjdk.tools.sjavac.Log;
import org.slf4j.MDC;
import org.testng.Assert;
import org.testng.SkipException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public class RestAssuredExtension {
    private static final Logger log = Logger.getLogger(RestAssuredExtension.class);
    public static RestAssuredPropertiesConfig configProperties = new RestAssuredPropertiesConfig();
    public static RequestSpecification request = null;
    public static ResponseOptions<Response> response = null;
    public static ResponseOptions<Response> middlewareCredentials = null;
    public static Response responseList;
    public static String token;
    public static String AccessKeyId;
    public static String SecretAccessKey;
    public static String SessionToken;
    public static JsonObject collectionVariables = new JsonObject();
    public static JsonObject userCognitoData = new JsonObject();
    public static JsonObject queryVariables = new JsonObject();
    public static JsonObject bashVariables = new JsonObject();
    public static JsonObject currentUserData = new JsonObject();
    public static RequestSpecBuilder builder = new RequestSpecBuilder();
    public static RequestSpecBuilder builderMW = new RequestSpecBuilder();
    public static ContentType content;
    public static AmazonDynamoDB amazonDynamoDB;
    public static Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    public static Map<String, String> expressionAttributeNames = new HashMap<>();
    public static ScanResult result;
    public static Table dynamoTable;
    public static DynamoDB dynamoDB;
    public static QuerySpec spec;
    public static ItemCollection<QueryOutcome> items;
    public static String sessionUser;
    public static String subId;
    public static String operationType;
    public static String overrideLanguage;
    public static int postCount = 0;
    public static boolean userDelete = false;
    public static JsonObject userDataMiddleware = new JsonObject();
    private static final int MAX_RETRIES = 1;
    private static final int INITIAL_TIMEOUT = 10000; // 10 seconds
    private static final int MAX_TIMEOUT = 30000;

    public RestAssuredExtension() {
        try {
            builder.setBaseUri(configProperties.getBaseUri());
            builder.setContentType(ContentType.JSON);
            dynamo(true);
            dynamoDBClient();
        } catch (IllegalArgumentException e) {
            log.info("Base URI cannot be null, check configProperties");
        }
    }

    public static String getSessionUser() {
        if (StringUtils.isNotEmpty(sessionUser)) {
            return sessionUser;
        }

        String defaultUser = determineDefaultUser();
        setSessionUser(defaultUser);

        return sessionUser;
    }

    public static String determineDefaultUser() {
        boolean isMiddlewareRunDirect = StringUtils.containsIgnoreCase(
                RestAssuredPropertiesConfig.getMiddlewareRunDirect(), "true");

        return isMiddlewareRunDirect
                ? configProperties.getDefaultMiddlewareUser()
                : getDefaultOrValidUser(null);
    }

    public boolean getCognitoUsers() {
        return configProperties.getCognitoUsers();
    }

    public String getDefaultUserWithAttributes(String attribute, Boolean value) {
        JsonObject usersBundle = getUserBundleData();

        if (usersBundle == null || usersBundle.entrySet().isEmpty()) {
            log.warn("No users found in the user bundle.");
            return null;
        }

        for (String userKey : usersBundle.keySet()) {
            JsonObject userData = usersBundle.getAsJsonObject(userKey);
            if (userHasAttribute(userData, attribute, value)) {
                sessionUser = userKey;
                break;
            }
        }

        return sessionUser;
    }

    private boolean userHasAttribute(JsonObject userData, String attribute, Boolean value) {
        return userData != null && userData.has(attribute) &&
                userData.get(attribute).getAsBoolean() == value;
    }

    public static String getSubIdByUsernameIntoLocalData(String attribute) {
        if (sessionUser == null) {
            sessionUser = getSessionUser();
        }

        JsonObject usersBundle = getUserBundleData();

        if (usersBundle == null || usersBundle.entrySet().isEmpty()) {
            log.warn("No users found in the user bundle.");
            return null;
        }

        String subId = findSubIdByUsername(usersBundle, attribute);

        if (subId == null) {
            UserType cognitoUser = getConfirmedUserCognito("CONFIRMED");
            subId = getAttributeUserCognito(cognitoUser, attribute);
        }

        saveInScenarioContext("subId", subId);
        return subId;
    }

    private static String findSubIdByUsername(JsonObject usersBundle, String attribute) {
        for (String userKey : usersBundle.keySet()) {
            JsonObject userData = usersBundle.getAsJsonObject(userKey);
            if (userData != null && userData.has(attribute) &&
                    StringUtils.containsIgnoreCase(userKey, sessionUser)) {
                return userData.get(attribute).getAsString();
            }
        }
        return null;
    }

    public static UserType getConfirmedUserCognito(String status) {
        List<UserType> listAllCognitoUsers = listAllUsers();
        UserType user = null;
        for (UserType cognitoUser : listAllCognitoUsers) {
            boolean isConfirmed = cognitoUser.userStatus().toString().equalsIgnoreCase(status)
                    && StringUtils.containsIgnoreCase(cognitoUser.username(), sessionUser);
            if (isConfirmed) {
                user = cognitoUser;
                break;
            }
        }
        return user;
    }

    public static String getAttributeUserCognito(UserType cognitoUser, String attributeName) {
        if (cognitoUser.hasAttributes()) {
            for (AttributeType attribute : cognitoUser.attributes()) {
                if (attribute.name().equals(attributeName)) {
                    return attribute.value();
                }
            }
        }
        return "";
    }

    public static String getDefaultUsername() {
        if (sessionUser == null) {
            sessionUser = getSessionUser();
        }
        if (!sessionUser.isEmpty()) {
            JsonObject usersBundle = getUserBundleData();
            for (String userKey : usersBundle.keySet()) {
                boolean isPresent = StringUtils.containsIgnoreCase(userKey, sessionUser);
                if (isPresent) {
                    username = userKey;
                    break;
                }
            }
        }
        UserType cognitoUser = getConfirmedUserCognito("CONFIRMED");
        username = cognitoUser.username();
        return username;
    }

    public boolean replacementAttributesFromData(String attribute, boolean value) {
        boolean isPresent = false;
        JsonObject usersBundle = getUserBundleData();
        for (String userKey : usersBundle.keySet()) {
            JsonObject userData = usersBundle.getAsJsonObject(userKey);
            isPresent = userData.has(attribute) && userData.get(attribute).getAsBoolean() == value;
            if (!isPresent) {
                sessionUser = userKey;
                break;
            }
        }
        return isPresent;
    }

    public static JsonObject getUserBundleData() {
        return configProperties.useCognitoUsers()
                ? getUserDataFromFile(COGNITO_USERS_FILE_LOCATION.getText())
                : getUserData();
    }

    public static JsonObject getUserData() {
        JsonObject usersBundle = null;
        String usersPath;
        try {
            if (StringUtils.containsIgnoreCase(RestAssuredPropertiesConfig.getMiddlewareRunDirect(), "true")) {
                usersPath =
                        new String(Files.readAllBytes(Paths.get(MIDDLEWARE_USERS_FILE_LOCATION.getText())));
            } else {
                usersPath = new String(Files.readAllBytes(Paths.get(USERS_FILE_LOCATION.getText())));
            }
            usersBundle = JsonParser.parseString(usersPath).getAsJsonObject();
        } catch (IOException | JsonSyntaxException e) {
            log.error(e.getMessage());
            Assert.assertTrue(StringUtils.isEmpty(e.toString()), e.toString());
        }
        return usersBundle;
    }

    public static String getDefaultOrValidUser(String priorUser) {
        List<UserType> listAllCognitoUsers = listAllUsers();
        JsonObject cognitoUsersBundle = getUserDataFromFile(COGNITO_USERS_FILE_LOCATION.getText());

        String[] listAllUsers = configProperties.useCognitoUsers() && cognitoUsersBundle != null &&
                !cognitoUsersBundle.isEmpty()
                ? cognitoUsersBundle.keySet().toArray(new String[0])
                : configProperties.getDefaultUser();

        if (StringUtils.isNotEmpty(priorUser) && !configProperties.getCognitoUsers()) {
            listAllUsers = addToArray(listAllUsers, priorUser);
        }

        sessionUser = "";
        if (listAllCognitoUsers.isEmpty()) {
            throw new SkipException("No users in this pool");
        }

        for (String possibleUser : listAllUsers) {
            for (UserType cognitoUser : listAllCognitoUsers) {
                if (StringUtils.containsIgnoreCase(cognitoUser.username(), possibleUser)) {
                    sessionUser = possibleUser;
                    collectionVariables = authenticationGraphQL("login.graphql");
                    if (collectionVariables.isEmpty()) {
                        log.info(
                                sessionUser + ": invalid credentials, unable to login, trying with a new one...");
                        MDC.put("user", sessionUser);
                    }
                    break;
                }
            }
            if (StringUtils.isNotEmpty(sessionUser) && !collectionVariables.isEmpty()) {
                break;
            }
        }

        if (StringUtils.isNotEmpty(sessionUser)) {
            return sessionUser;
        } else {
            throw new SkipException("No credentials available");
        }
    }


    private static String[] addToArray(String[] arr, String element) {
        String[] newArr;
        if (!StringUtils.containsIgnoreCase(arr[0], element)) {
            newArr = new String[arr.length + 1];
            newArr[0] = element;
            System.arraycopy(arr, 0, newArr, 1, arr.length);
        } else {
            log.info("Prior user is already present on the list");
            newArr = arr;
        }
        return newArr;
    }

    public static String setSessionUser(String user) {
        if (!StringUtils.containsIgnoreCase(RestAssuredPropertiesConfig.getMiddlewareRunDirect(), "true")) {
            sessionUser = getDefaultOrValidUser(user);
            return sessionUser;
        } else {
            if (StringUtils.containsIgnoreCase(configProperties.getDefaultMiddlewareUser(), user)) {
                sessionUser = configProperties.getDefaultMiddlewareUser();
                return sessionUser;
            } else {
                sessionUser = setMiddlewareUserData(user);
                return sessionUser;
            }
        }
    }

    public static String setMiddlewareUserData(String user) {
        JsonObject usersBundleMiddleware = getUserData();
        for (String userKey : usersBundleMiddleware.keySet()) {
            if (StringUtils.containsIgnoreCase(userKey, user)) {
                sessionUser = userKey;
                userDataMiddleware = usersBundleMiddleware.getAsJsonObject(userKey);
                break;
            }
        }
        return sessionUser;
    }

    public static String setOperationType(String type) {
        operationType = type;
        return operationType;
    }

    public static Path _path(String path) {
        return Paths.get(configProperties.getBodyData() + path);
    }

    public static String getOperationType() {
        return operationType;
    }


    /**
     * return string value of a text in a file
     *
     * @param path path to file at src/test/resources/data/body
     */
    public static String getBodyFromResource(String path) {
        String bodyPath;
        try {
            bodyPath = new String(Files.readAllBytes(_path(path)));
            return bodyPath;
        } catch (IOException | NullPointerException e) {
            log.info("check configProperties or path variable");
            return null;
        }
    }

    public static String generateBodyFromResource(String path, String variablePath) {
        StringBuilder stringBuilder = new StringBuilder();
        String body = getBodyFromResource(path);
        JsonObject variableJson = createJsonParameters(variablePath);

        Pattern pattern = Pattern.compile("\\$(\\w+)");
        assert body != null;
        Matcher matcher = pattern.matcher(body);
        String replacement;

        while (matcher.find()) {
            String varName = matcher.group(1);
            replacement = "";

            if (variableJson.has(varName)) {
                replacement = variableJson.get(varName).getAsString();
            }

            replacement = handleSpecialCases(varName, replacement, variableJson);

            if (StringUtils.isNotEmpty(replacement)) {
                matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(stringBuilder);
        body = stringBuilder.toString();
        log.info(body);
        return body;
    }

    private static String handleSpecialCases(String varName, String replacement,
                                             JsonObject _variableJson) {
        if (replacement.contains("Today") || varName.contains("Today")) {
            return getTodayDate(yyyyMMddPattern);
        }
        if (replacement.contains("Yesterday") || varName.contains("Yesterday")) {
            return dateSubtraction(1);
        }
        if (replacement.contains("WeekAgo") || varName.contains("WeekAgo")) {
            return dateSubtraction(7);
        }
        if (replacement.contains("MonthAgo") || varName.contains("MonthAgo")) {
            return dateSubtraction(30);
        }
        if (replacement.contains("RandomNumber") || varName.contains("RandomNumber")) {
            return randomNumber(50) + ".01";
        }
        if (replacement.contains("TypeOperation") || varName.contains("TypeOperation")) {
            return randomOperation();
        }
        if (varName.contains("toDate")) {
            String datePeriod = getDateIntoPeriod("previousDate", "to", _variableJson);
            return StringUtils.isNotEmpty(datePeriod) ? datePeriod : getTodayDate();
        }
        if (varName.contains("fromDate")) {
            String datePeriod = getDateIntoPeriod("previousDate", "from", _variableJson);
            return StringUtils.isNotEmpty(datePeriod) ? datePeriod : dateSubtraction(300);
        }
        return replacement;
    }

    public static String generateBodyFromResource(String path) {
        String body = getBodyFromResource(path);
        if (StringUtils.isEmpty(body)) {
            throw new IllegalArgumentException("The path of body is invalid");
        }

        if (!StringUtils.containsIgnoreCase(path, "authenticationMiddlewareCERT.json")) {
            JsonObject currentUserData = getCurrentUserData();
            JsonObject overrideData = AbstractAPI.overrideData;
            JsonObject scenarioData = AbstractAPI.scenarioData;

            if (Objects.nonNull(overrideData) || Objects.nonNull(scenarioData) ||
                    Objects.nonNull(currentUserData)) {
                body = applyOverridesAndScenarioData(body, overrideData, scenarioData);
            }

            body = replaceVariables(body, currentUserData);
        }

        return body;
    }

    private static String replaceVariables(String input, JsonObject currentUserData) {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(input);

        StringBuilder resultBuilder = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = handleSpecialCases(varName, currentUserData);

            if (StringUtils.isNotEmpty(replacement)) {
                matcher.appendReplacement(resultBuilder, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(resultBuilder);

        String result = resultBuilder.toString();
        log.info("Login with ecordero29");
        return result;
    }

    private static String applyOverridesAndScenarioData(String body, JsonObject overrideData,
                                                        JsonObject scenarioData) {
        if (overrideData != null) {
            body = setOverrideAndSavedData(body, overrideData);
        }

        if (scenarioData != null) {
            body = setOverrideAndSavedData(body, scenarioData);
        }

        return body;
    }


    private static String handleSpecialCases(String varName, JsonObject currentUserData) {
        String replacement = "";

        try {
            if (!currentUserData.isEmpty()) {
                JsonObject userData = currentUserData.getAsJsonObject("userData");
                JsonObject apiDetails = currentUserData.getAsJsonObject("apiDetails");
                if (userData.has(varName) &&
                        StringUtils.isNotEmpty(userData.get(varName).getAsString())) {
                    replacement = userData.get(varName).getAsString();
                }
                if (apiDetails.has(varName) &&
                        StringUtils.isNotEmpty(apiDetails.get(varName).getAsString())) {
                    replacement = apiDetails.get(varName).getAsString();
                }
            }
//            if (currentUserData.has(varName) &&
//                    StringUtils.isNotEmpty(currentUserData.get(varName).getAsString())) {
//                replacement = currentUserData.get(varName).getAsString();
//            }
            else {
                replacement = setReplacement(currentUserData, varName);
            }
        } catch (Exception e) {
            log.error(String.format("The entity %s is not present in currentUserData: ", varName) + e);
        }

        switch (varName) {
            case "Today":
                replacement = getTodayDate(yyyyMMddPattern);
                break;
            case "toDate":
                replacement = getDateIntoPeriod("previousDate", "to");
                replacement =
                        StringUtils.isNotEmpty(replacement) ? replacement : getTodayDate(yyyyMMddPattern);
                break;
            case "WeekAgo":
                replacement = dateSubtraction(7);
                break;
            case "Yesterday":
                replacement = dateSubtraction(1);
                break;
            case "MonthAgo":
                replacement = dateSubtraction(30);
                break;
            case "fromDate":
                replacement = getDateIntoPeriod("previousDate", "from");
                replacement = StringUtils.isNotEmpty(replacement) ? replacement : dateSubtraction(300);
                break;
            case "RandomNumber":
                replacement = randomNumber(50) + ".01";
                break;
            case "TypeOperation":
                replacement = randomOperation();
                break;
            case "Username":
                replacement = randomUsername(8);
                break;
            case "Password":
                replacement = randomPassword(12);
                break;
            case "UpdatePasswd":
                replacement = randomPassword(12);
                setOverwriteUpdatePassword(replacement);
                log.info(replacement);
                break;
            case "currentPassword":
                JsonArray userData = (JsonArray) currentUserData.get("login");
                replacement = userData.get(1).getAsString();
                log.info(replacement);
                break;
            default:
                break;
        }

        return replacement;
    }

    /**
     * Replaces placeholders in the input string with values from the JsonObject.
     *
     * @param body The input string containing placeholders.
     * @param data The JsonObject containing values to replace placeholders.
     * @return The modified string with placeholders replaced by values.
     */
    public static String setOverrideAndSavedData(String body, JsonObject data) {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(body);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            JsonElement element = data.get(varName);
            String replacement = (element != null && !element.isJsonNull()) ? element.getAsString() : "";

            if (StringUtils.isNotEmpty(replacement)) {
                matcher.appendReplacement(result, replacement);
            }
        }

        matcher.appendTail(result);

        return result.toString();
    }

    protected static String setReplacement(JsonObject currentUserData, String varName) {
        String replacement = "";

        if (varName.contains("cardId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("customerId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("institutionId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }
        }

        if (varName.contains("ssnLastFourDigits")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("lastEightDigits")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("expirationDate")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("traceId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("routeId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("customerIdType")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("accountId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("accountType")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("merchantCategoryCode")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("merchantCategoryDescription")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("description")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("cardId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("phone")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }

        }

        if (varName.contains("encryptedCardId")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }
        }

        if (varName.contains("cardType")) {
            if (currentUserData != null) {
                replacement = getReplacement(currentUserData, varName);
            } else {
                replacement = getReplacement(getCurrentUserDataByUsername(sessionUser), varName);
            }
        }

        return replacement;
    }


    public static String getReplacement(JsonObject currentUserData, String varName) {
        String replacement = "";

        if (currentUserData != null && currentUserData.has(varName)) {
            replacement = currentUserData.get(varName).getAsString();
        } else if (userCognitoData != null && userCognitoData.has(varName)) {
            replacement = userCognitoData.get(varName).getAsString();
        }

        return replacement;
    }


    public static String generateBodyFromResource(String path, JsonObject variablePath) {
        StringBuffer stringbuffer = new StringBuffer();
        String _body = getBodyFromResource(path);
        Pattern pattern = Pattern.compile("\\$(\\w+)");

        if (StringUtils.isEmpty(_body)) {
            throw new NullPointerException("body is empty");
        }
        Matcher matcher = pattern.matcher(_body);
        String replacement = null;
        while (matcher.find()) {
            String varName = matcher.group(1);
            try {
                replacement = variablePath.get(varName).toString().replace("\"", "");
            } catch (NullPointerException e) {
                log.error(e.getMessage());
                log.info("replacement:353 entity not found");
            }

            if (varName.equalsIgnoreCase("Today")) {
                replacement = getTodayDate(yyyyMMddPattern);
            }
            if (varName.equalsIgnoreCase("WeekAgo")) {
                replacement = dateSubtraction(7);
            }
            if (varName.equalsIgnoreCase("RandomNumber")) {
                replacement = randomNumber(50) + ".01";
            }
            if (varName.equalsIgnoreCase("TypeOperation")) {
                replacement = randomOperation();
            }
            if (StringUtils.isNotEmpty(replacement)) {
                try {
                    matcher.appendReplacement(
                            stringbuffer,
                            matcher.group(1).replaceFirst(Pattern.quote(matcher.group(1)), replacement));
                } catch (Exception e) {
                    log.info(e);
                }
            }
        }
        matcher.appendTail(stringbuffer);
        _body = stringbuffer.toString();
        log.info(_body);
        return _body;
    }

    public static String getDateIntoPeriod(String period, String key) {
        return replacementAttributesFromData(period, key);
    }

    public static String replacementAttributesFromData(String attribute, String value) {
        JsonObject usersBundle = StringUtils.containsIgnoreCase(getMiddlewareRunDirect(), "true")
                ? getUserData()
                : getUserBundleData();

        return usersBundle.entrySet().stream()
                .filter(entry -> StringUtils.containsIgnoreCase(entry.getKey(), sessionUser))
                .map(Map.Entry::getValue)
                .map(JsonElement::getAsJsonObject)
                .findFirst()
                .map(userData -> findReplacement(userData, attribute, value))
                .orElse("");
    }

    public static String findReplacement(JsonObject userData, String attribute, String value) {

        if (StringUtils.containsIgnoreCase(getMiddlewareRunDirect(), "true")) {
            return findReplacementMiddlewareData(userData, attribute, value);
        } else {
            return findReplacementCognitoData(userData, attribute, value);
        }
    }

    public static String findReplacementCognitoData(JsonObject userData, String attribute, String value) {
        JsonArray cardDataArray = userData.getAsJsonArray("cardData");
        if (cardDataArray == null || cardDataArray.isEmpty()) {
            return "";
        }

        if (cardDataArray.size() == 1) {
            JsonObject primerCard = cardDataArray.get(0).getAsJsonObject();
            if (primerCard.has(attribute)) {
                JsonObject periodDate = primerCard.getAsJsonObject(attribute);
                if (periodDate.has(value)) {
                    return periodDate.get(value).getAsString();
                }
            }
        } else {
            for (JsonElement cardElement : cardDataArray) {
                JsonObject card = cardElement.getAsJsonObject();
                return value;
            }
        }


        return "";
    }

    public static String findReplacementMiddlewareData(JsonObject userData, String attribute, String value) {
        JsonArray cardDataArray = userData.getAsJsonArray("apiDetails");
        if (cardDataArray == null || cardDataArray.isEmpty()) {
            return "";
        }

        if (cardDataArray.size() == 1) {
            JsonObject primerCard = cardDataArray.get(0).getAsJsonObject();
            if (primerCard.has(attribute)) {
                JsonObject periodDate = primerCard.getAsJsonObject(attribute);
                if (periodDate.has(value)) {
                    return periodDate.get(value).getAsString();
                }
            }
        } else {
            for (JsonElement cardElement : cardDataArray) {
                JsonObject card = cardElement.getAsJsonObject();
                return value;
            }
        }


        return "";
    }


    public static String getDateIntoPeriod(String period, String key, JsonObject variablePath) {
        String datePeriod = "";
        try {
            if (variablePath != null) {
                JsonArray cardDataArray = variablePath.getAsJsonArray("cardData");
                if (cardDataArray != null && !cardDataArray.isEmpty()) {
                    JsonObject det = cardDataArray.get(0).getAsJsonObject();
                    JsonObject detPeriod = det.getAsJsonObject(period);
                    if (detPeriod != null && detPeriod.has(key)) {
                        datePeriod = detPeriod.get(key).getAsString();
                    }
                }
            }
        } catch (Exception e) {
            log.info("Could not load card details");
        }
        return datePeriod;
    }

    public static JsonObject createJsonParameters(String variablePath) {
        if (queryVariables == null) {
            try {
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(generateBodyFromResource(variablePath));
                queryVariables = jsonElement.getAsJsonObject();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return queryVariables;
    }

    public static void generateBearerToken(boolean newToken) {
        try {
            if (shouldGenerateNewToken(newToken)) {
                if (isPostCountReset()) {
                    setSessionUserIfEmpty();
                    loadCollectionVariablesIfEmpty();
                    processToken();
                }
            } else {
                processExistingToken();
            }

            logTokenInfo();
        } catch (Exception e) {
            log.error("Error generating Bearer token: " + e.getMessage(), e);
        }
    }

    private static boolean shouldGenerateNewToken(boolean newToken) {
        return newToken || !userDelete;
    }

    private static boolean isPostCountReset() {
        return postCount == 0 || postCount >= 10 || !userDelete;
    }

    private static void setSessionUserIfEmpty() {
        if (StringUtils.isEmpty(sessionUser)) {
            sessionUser = setSessionUser("");
        }
    }

    private static void loadCollectionVariablesIfEmpty() {
        if (collectionVariables == null || collectionVariables.isEmpty()) {
            collectionVariables = authenticationGraphQL("login.graphql");
        }
    }

    private static void processToken() {
        if (collectionVariables != null && !collectionVariables.isEmpty()) {
            String idToken = removeCharacter(collectionVariables.get("IdToken").toString());
            String usernameToken = getUsernameDecodeJWToken(idToken);

            if (shouldUseScenarioDataToken(usernameToken)) {
                String tokenIdScenarioData = scenarioData.get("IdToken").getAsString();
                token = "Bearer " + tokenIdScenarioData;
            } else {
                token = "Bearer " + idToken;
            }

            builder.addHeader("Authorization", token);
            postCount = (postCount + 1) % 10; // Reset postCount after 10 requests
        } else {
            token = null;
        }
    }

    private static boolean shouldUseScenarioDataToken(String usernameToken) {
        if (!scenarioData.isEmpty() && scenarioData.has("IdToken")) {
            String tokenIdScenarioData = scenarioData.get("IdToken").getAsString();
            String usernameTemporary = getUsernameDecodeJWToken(tokenIdScenarioData);
            return usernameToken.contains(sessionUser) &&
                    "Automation2023".equalsIgnoreCase(usernameTemporary);
        }
        return false;
    }

    private static void processExistingToken() {
        if (!collectionVariables.isEmpty() && scenarioData.has("IdToken") && token == null) {
            token = "Bearer " + scenarioData.get("IdToken").getAsString();
            builder.addHeader("Authorization", token);
            postCount = (postCount + 1) % 10;
        } else if (token == null && userDelete) {
            token = "Bearer " + collectionVariables.get("IdToken").toString();
            builder.addHeader("Authorization", token);
            postCount = (postCount + 1) % 10;
        }
    }

    private static void logTokenInfo() {
        if (token != null) {
            log.info(getUsernameDecodeJWToken(token.replace("Bearer ", "")));
        }
    }

    public static void cleanUserTokens(String user) {
        if (token == null) {
            log.info("Token is null");
            return;
        }

        try {
            String cleanToken = removeBearer(token);
            String tokenUsername = getUsernameDecodeJWToken(cleanToken);

            if (StringUtils.equalsIgnoreCase(tokenUsername, user)) {
                userDelete = true;
                logDeletion(tokenUsername, cleanToken);
            }
        } catch (Exception e) {
            log.error("Error cleaning user tokens: " + e.getMessage(), e);
        }
    }

    private static String removeBearer(String token) {
        if (token.contains("Bearer")) {
            return token.replace("Bearer ", "");
        }
        return token;
    }

    private static void logDeletion(String username, String token) {
        log.info("Delete {} " + username);
        log.info("Delete token {} " + token);
    }


    public static String getUsernameDecodeJWToken(String idToken) {
        if (StringUtils.isBlank(idToken)) {
            throw new IllegalArgumentException("ID token cannot be null or empty");
        }
        try {
            String cleanedToken = removeCharacter(idToken);
            DecodedJWT jwt = decodeToken(cleanedToken);
            return extractUsername(jwt);
        } catch (JWTDecodeException e) {
            logAndThrowException("Failed to decode JWT token", e);
        }
        return null;
    }

    public static String getRoleDecodeJWToken(String idToken) {
        if (StringUtils.isBlank(idToken)) {
            throw new IllegalArgumentException("ID token cannot be null or empty");
        }

        try {
            String cleanedToken = removeCharacter(idToken);
            DecodedJWT jwt = decodeToken(cleanedToken);
            return extractPreferredRole(jwt);
        } catch (JWTDecodeException e) {
            logAndThrowException("Failed to decode JWT token", e);
        }

        return null;
    }

    public static String removeCharacter(String token) {
        return token.replaceAll("\"", "");
    }

    private static DecodedJWT decodeToken(String token) {
        return JWT.decode(token);
    }

    private static String extractPreferredRole(DecodedJWT jwt) {
        String preferredRole = jwt.getClaim("cognito:preferred_role").asString();
        if (StringUtils.isBlank(preferredRole)) {
            throw new IllegalStateException("Preferred role not found in JWT token");
        }
        log.info("Preferred role: {} " + preferredRole);
        return preferredRole;
    }

    private static String extractUsername(DecodedJWT jwt) {
        String username = jwt.getClaim("cognito:username").asString();
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("Preferred role not found in JWT token");
        }
        log.info("Preferred Username: {} " + username);
        return username;
    }

    private static void logAndThrowException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }


    /**
     * Get bearer token and put in header request
     */
    public static void generateMiddlewareToken(boolean newToken) {
        if (middlewareCredentials == null || newToken) {
            middlewareCredentials = getMiddlewareCredentials();
        }

        try {
            boolean hasAccessId = false;
            boolean hasAccessToken = false;

            RequestSpecificationImpl spec = (RequestSpecificationImpl) builderMW.build();

            for (Header header : spec.getHeaders()) {
                if (StringUtils.containsIgnoreCase(header.getName(), "X-Access-Id")) {
                    hasAccessId = true;
                }
                if (StringUtils.containsIgnoreCase(header.getName(), "X-Access-Token")) {
                    hasAccessToken = true;
                }
            }

            if (!hasAccessId) {
                builderMW.addHeader("X-Access-Id", middlewareCredentials.getHeader("X-Access-Id"));
            }
            if (!hasAccessToken) {
                builderMW.addHeader("X-Access-Token", middlewareCredentials.getHeader("X-Access-Token"));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Put in header request language parameter
     */
    public static void generateAWSHeadersCredentials() {
        if (bashVariables.isEmpty()) {
            getAWSCredentials();
        }
        try {
            AccessKeyId = runBashProcess("get_aws_access_key_id");
            SecretAccessKey = runBashProcess("get_aws_secret_access_key");
            SessionToken = runBashProcess("get_aws_session_token");
            log.info(AccessKeyId);
            log.info(SecretAccessKey);
            log.info(SessionToken);
            builder.addHeader("AccessKeyId", AccessKeyId);
            builder.addHeader("SecretAccessKey", SecretAccessKey);
            builder.addHeader("SessionToken", SessionToken);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * put default header to request
     */
    public static void setDefaultHeaders() {
        if (Objects.nonNull(builder)) {
            builder.addHeader("Content-Type", "application/json; charset=utf-8");
        }
    }

    /**
     * Set a body from file and set as specific graphQL body content
     *
     * @param bodyFile     set body file using generateBodyFromResource function
     * @param variablePath set parameter to body file using generateBodyFromResource function
     */
    public static RequestSpecBuilder setBodyGraphql(String bodyFile, String variablePath) {
        JsonObject query = new JsonObject();
        try {
            query.addProperty("query", generateBodyFromResource(bodyFile, variablePath));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return builder.setBody(query.toString());
    }

    public static RequestSpecBuilder setBodyGraphql(String bodyFile, JsonObject variable) {
        JsonObject query = new JsonObject();
        try {
            if (variable != null)
                query.addProperty("query", generateBodyFromResource(bodyFile, variable));
            else {
                query.addProperty("query", generateBodyFromResource(bodyFile));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return builder.setBody(query.toString());
    }

    /**
     * Set a body from file and set as specific graphQL body content
     *
     * @param bodyFile set body file using generateBodyFromResource function
     */
    public static RequestSpecBuilder setBodyGraphql(String bodyFile) {
        JsonObject query = new JsonObject();
        try {
            query.addProperty("query", generateBodyFromResource(bodyFile));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return builder.setBody(query.toString());
    }

    /**
     * set a content type
     *
     * @param type on format that you needed, text, json, html...
     */
    private static ContentType setContentType(String type) {
        switch (type) {
            case "TEXT":
                content = ContentType.TEXT;
                break;
            case "JSON":
                content = ContentType.JSON;
                break;
            case "HTML":
                content = ContentType.HTML;
                break;
            case "ANY":
                content = ContentType.ANY;
                break;
            case "XML":
                content = ContentType.XML;
                break;
        }
        return content;
    }

    public static JsonObject selectCognitoUser() {
        JsonObject usersBundle;
        String clientId;
        JsonArray userData;
        usersBundle = getCurrentUserData();

        if (getUserData().has(sessionUser)) {
            usersBundle = getCurrentUserDataByUsername(sessionUser);
        }

        try {
            if (usersBundle != null) {
                userData = usersBundle.getAsJsonArray("login");
                clientId = configProperties.getClientId();
                userCognitoData.addProperty("USERNAME", userData.get(0).getAsString());
                userCognitoData.addProperty("PASSWORD", setPasswordBase());
                userCognitoData.addProperty("ClientId", clientId);
            }
        } catch (Exception e) {
            log.debug("Skipping test because user is not available");
        }
        return userCognitoData;
    }

    public static JsonObject selectCognitoUser(String attributeName, String attributeValue) {
        JsonObject usersBundle;
        String clientId;
        JsonArray userData;
        username = filterUsersByAttribute(attributeName, attributeValue);
        usersBundle = getUserData();

        if (getUserData().has(sessionUser)) {
            usersBundle = getCurrentUserDataByUsername(sessionUser);
        }

        try {
            userData = usersBundle.getAsJsonArray("login");
            if (!userData.get(0).getAsString().equalsIgnoreCase(username)) {
                userData.set(0, new JsonParser().parse(username));
            }
            clientId = configProperties.getClientId();
            userCognitoData.addProperty("USERNAME", userData.get(0).getAsString());
            userCognitoData.addProperty("PASSWORD", setPasswordBase());
            userCognitoData.addProperty("ClientId", clientId);
        } catch (Exception e) {
            throw new SkipException("Skipping test because user is not available");
        }
        return userCognitoData;
    }

    public static void setOverwriteUpdatePassword(String password) {
        String path = "src/test/resources/data/users.json";
        String usersPath = null;
        try {
            usersPath = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        JsonObject json = new JsonObject();
        try {
            if (Objects.nonNull(usersPath)) {
                json = JsonParser.parseString(usersPath).getAsJsonObject();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        JsonObject dataUser = json.getAsJsonObject("automation");
        JsonArray loginData = dataUser.getAsJsonArray("login");
        loginData.set(1, new JsonParser().parse(password));
        dataUser.add("login", loginData);
        json.add("automation", dataUser);

        writtenBodyFromResource(path, json.toString());
    }

    public static void writtenBodyFromResource(String path, String text) {
        Path fileName = Path.of(path);
        try {
            Files.writeString(fileName, text);
            String file_content = Files.readString(fileName);
            log.info(file_content);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static boolean getAWSCredentials() {
        try {
            runBashProcess("AWS_ACCESS_KEY_ID");
            runBashProcess("AWS_SECRET_ACCESS_KEY");
            runBashProcess("AWS_SESSION_TOKEN");
            return true;
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error(e.getMessage());
            return false;
        }
    }


    /**
     * Helper method to build and send POST requests to the middleware with or without a request body.
     */
    private static ResponseOptions<Response> sendPostRequest(String path, RequestSpecBuilder builderM) {
        int retries = 0;
        int timeout = INITIAL_TIMEOUT;

        while (retries < MAX_RETRIES) {
            try {

                builderM.setConfig(RestAssured.config());
                request = RestAssured.given().spec(builderM.build());
                response = request.post(new URI(path));

                log.info("Response: " + response.getBody().prettyPrint());

                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    log.info(response.getBody().prettyPrint() + "This is the successful request");
                    return response;
                } else {
                    log.warn("Request failed with status code: " + response.getStatusCode());
                }
            } catch (Exception e) {
                log.warn("Request failed. Retrying... (Attempt " + (retries + 1) + " of " + MAX_RETRIES + ")");
                log.warn("Error: " + e.getMessage());
            }

            timeout = Math.min(timeout * 2, MAX_TIMEOUT);
            retries++;

           try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted during retry wait", ie);
            }
        }
        return response;
    }


    /**
     * Builds the base request spec and configures proxy if necessary.
     */
    private static RequestSpecBuilder buildRequestSpec(String path, String body) {
        setDefaultHeaders();

        if (configProperties.setUseMicroserviceProxyMiddleware()) {
            builderMW.setBaseUri(configProperties.getHostMiddleWareProxy()
                    .concat(configProperties.getMiddlewareProxyEndpoint()));
            log.info("Using Middleware Proxy: " + configProperties.getHostMiddleWareProxy());
        } else {
            builderMW.setBaseUri(configProperties.getHostMiddleWare());
            setProxyMiddleware();
            generateMiddlewareToken(true);
            log.info("Using Middleware: " + configProperties.getHostMiddleWare() + "/" + path);
        }

        if (body != null) {
            builderMW.setBody(generateBodyFromResource(body)).setContentType(ContentType.TEXT);
        }
        builderMW.setAccept(ContentType.JSON);
        builderMW.setContentType(ContentType.JSON);

        return builderMW;
    }

    /**
     * POST method with body and path.
     */
    public static ResponseOptions<Response> postMethodMiddleware(String path, String body) {
        try {
            builderMW = buildRequestSpec(path, body);
            response = sendPostRequest(path, builderMW);
            return response;
        } catch (Exception e) {
            log.error("Middleware connection error: " + e);
            throw new SkipException("Middleware connection error: " + e);
        }
    }

    /**
     * POST method with path only (no body).
     */
    public static ResponseOptions<Response> postMethodMiddleware(String path) {
        return postMethodMiddleware(path, null);
    }

    /**
     * POST method with body, path, and additional variable for body generation.
     */
    public static ResponseOptions<Response> postMethodMiddleware(String path, String body,
                                                                 String variable) {
        try {
            RequestSpecBuilder builderMW = buildRequestSpec(path, body);
            builderMW.setBody(generateBodyFromResource(body, variable));
            return sendPostRequest(path, builderMW);
        } catch (Exception e) {
            log.error("Middleware connection error: " + e);
            throw new SkipException("Middleware connection error: " + e);
        }
    }


    public static void setProxyEnv() {
        if (StringUtils.equalsIgnoreCase("EVT-DEV-PRV", getEnvironment())) {
            builder.setProxy(configProperties.getSetProxyAddress(), configProperties.getSetProxyPort());
            builder.addHeader("X-Frame-Options", "DENY");
            builder.addHeader("Content-Security-Policy", "frame-ancestors 'none';");
            builder.addHeader("X-Content-Type-Options", "nosniff");
            builder.addHeader("Cache-Control", "no-cache");
        }
    }

    public static void setProxyMiddleware() {
        if (StringUtils.containsIgnoreCase(getEnvironmentMiddleware(), "APIG")) {
            builderMW.setProxy(configProperties.getSetProxyAddress(), configProperties.getSetProxyPort());
        }
    }

    public static void setProxyEnv(RequestSpecBuilder authBuilder) {
        if (StringUtils.equalsIgnoreCase("EVT-DEV-PRV", getEnvironment())) {
            authBuilder.setProxy(configProperties.getSetProxyAddress(),
                    configProperties.getSetProxyPort());
            authBuilder.addHeader("X-Frame-Options", "DENY");
            authBuilder.addHeader("Content-Security-Policy", "frame-ancestors 'none';");
            authBuilder.addHeader("X-Content-Type-Options", "nosniff");
            authBuilder.addHeader("Cache-Control", "no-cache");

        }
    }

    /**
     * get response from GraphQL Api
     *
     * @param body a text file with query/mutation schema.
     * @return Api responses
     */
    public static ResponseOptions<Response> postMethodGraphQL(String body) {
        response = null;
        request = null;
        builder = new RequestSpecBuilder();
        RestAssured.given()
                .config(RestAssured.config()
                        .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", 80000) // Connection timeout in milliseconds
                                .setParam("http.socket.timeout", 80000))); // Socket read timeout in milliseconds
        boolean isIdToken = body.contains("login");
        setDefaultHeaders();

        if (!isUserCreation) {
            generateBearerToken(StringUtils.isEmpty(sessionUser));
        }
        builder.addHeader("lang", setLanguage());
        setBodyGraphql(body);
        try {
            builder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv();
            request = RestAssured.given().spec(builder.build());
            if (StringUtils.equalsIgnoreCase(getEnvironment(), "EVT-CRT")) {
                response = request.post(new URI(configProperties.getBaseUri()));
            } else {
                response = request.post(new URI(configProperties.getGraphqlEndpoint()));
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }

        boolean condition = response.getStatusCode() == 200 || response.getStatusCode() != 200;
        shortWait(60, condition);

        if (isIdToken) {
            try {
                boolean errorsFound =
                        StringUtils.containsIgnoreCase(
                                response.getBody().asString(), ERROR_AN_UNEXPECTED_ERROR_OCURRED)
                                || StringUtils.containsIgnoreCase(
                                response.getBody().asString(), ERROR_NO_PUDIERON_VALIDARSE_LOS_DATOS_INGRESADOS)
                                || StringUtils.containsIgnoreCase(
                                response.getBody().asString(), ERROR_THE_DATA_PROVIDED_IS_INCORRECT)
                                || StringUtils.containsIgnoreCase(
                                response.getBody().asString(), ERROR_HA_OCURRIDO_UN_ERROR_INESPERADO);
                if (response.getStatusCode() == 200 && !errorsFound) {
                    if (response.getBody().jsonPath().get("data.login.idToken") != null) {
                        saveInScenarioContext("IdToken",
                                response.getBody().jsonPath().get("data.login.idToken").toString());
                    }
                } else {
                    log.error(response.getBody().asString());
                    log.info(response.getBody().asString() + " Errors found retrieving data");
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                log.error(e.getMessage());
            }
        }
        return response;
    }

    public static JsonObject authenticationGraphQL(String body) {
        ResponseOptions<Response> responseToken = null;
        RequestSpecBuilder authBuilder = new RequestSpecBuilder();
        setDefaultHeaders();
        JsonObject user = selectCognitoUser();
        JsonObject query = new JsonObject();

        if (user == null || user.isEmpty()) {
            user = selectCognitoUser();
        }

        query.addProperty("query", generateBodyFromResource(body, user));
        authBuilder.setBody(query.toString());

        try {
            authBuilder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv();
            RequestSpecification requestToken = RestAssured.given().spec(authBuilder.build());
            responseToken = requestToken.post(new URI(configProperties.getGraphqlEndpoint()));
            AbstractAPI.response = responseToken;

            boolean errorsFound = StringUtils.containsIgnoreCase(responseToken.getBody().asString(),
                    ERROR_AN_UNEXPECTED_ERROR_OCURRED)
                    || StringUtils.containsIgnoreCase(responseToken.getBody().asString(),
                    ERROR_NO_PUDIERON_VALIDARSE_LOS_DATOS_INGRESADOS);

            if (responseToken.getStatusCode() == 200 && !errorsFound) {
                collectionVariables.addProperty("IdToken",
                        responseToken.getBody().jsonPath().getString("data.login.idToken"));
                collectionVariables.addProperty("AccessToken",
                        responseToken.getBody().jsonPath().getString("data.login.accessToken"));
                collectionVariables.addProperty("refreshToken",
                        responseToken.getBody().jsonPath().getString("data.login.refreshToken"));
            } else {
                log.error(responseToken.getBody().asString());
                log.info(responseToken.getBody().asString() + " Errors found retrieving data");
                collectionVariables = new JsonObject();
            }
        } catch (Exception e) {
            collectionVariables = new JsonObject();
            if (responseToken != null) {
                throw new SkipException(
                        responseToken.getBody().asString() + " Errors found retrieving data " + e);
            } else {
                throw new SkipException("Errors found retrieving data " + e);
            }
        }

        return collectionVariables;
    }

    public static JsonObject authenticationGraphQL(JsonObject user, String body) {
        ResponseOptions<Response> responseToken = null;
        RequestSpecBuilder authBuilder = new RequestSpecBuilder();
        setDefaultHeaders();
        JsonObject query = new JsonObject();
        query.addProperty("query", generateBodyFromResource(body, user));
        authBuilder.setBody(query.toString());

        try {
            authBuilder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv(authBuilder);

            RequestSpecification requestToken = RestAssured.given().spec(authBuilder.build());
            responseToken = requestToken.post(new URI(configProperties.getGraphqlEndpoint()));
            AbstractAPI.response = responseToken;

            boolean errorsFound = StringUtils.containsIgnoreCase(responseToken.getBody().asString(),
                    ERROR_AN_UNEXPECTED_ERROR_OCURRED)
                    || StringUtils.containsIgnoreCase(responseToken.getBody().asString(),
                    ERROR_NO_PUDIERON_VALIDARSE_LOS_DATOS_INGRESADOS);

            if (responseToken.getStatusCode() == 200 && !errorsFound) {
                collectionVariables.addProperty("IdToken",
                        responseToken.getBody().jsonPath().getString("data.login.idToken"));
                collectionVariables.addProperty("AccessToken",
                        responseToken.getBody().jsonPath().getString("data.login.accessToken"));
                collectionVariables.addProperty("refreshToken",
                        responseToken.getBody().jsonPath().getString("data.login.refreshToken"));
            } else {
                log.error(responseToken.getBody().asString());
                log.info(responseToken.getBody().asString() + " Errors found retrieving data");
                collectionVariables = new JsonObject();
            }
        } catch (Exception e) {
            collectionVariables = new JsonObject();
            if (StringUtils.equalsIgnoreCase("EVT-CRT", getEnvironment())) {
                throw new SkipException("You are not connected to the VPN " + e);
            }
            if (responseToken != null) {
                throw new SkipException(
                        responseToken.getBody().asString() + " Errors found retrieving data " + e);
            } else {
                throw new SkipException("Errors found retrieving data " + e);
            }
        }

        return collectionVariables;
    }

    public static String setLanguage() {
        return StringUtils.isNotEmpty(overrideLanguage)
                ? overrideLanguage
                : configProperties.getLanguage();
    }

    /**
     * get response from GraphQL Api
     *
     * @param body         a text file with query/mutation schema.
     * @param variablePath file with variables.
     * @return Api responses
     */
    public static ResponseOptions<Response> postMethodGraphQL(String body, String variablePath) {
        response = null;
        builder = new RequestSpecBuilder();
        setDefaultHeaders();
        generateBearerToken(true);
        setBodyGraphql(body, variablePath);
        try {
            builder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv();
            request = RestAssured.given().spec(builder.build());
            response = request.post(new URI(configProperties.getGraphqlEndpoint()));
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        return response;
    }

    public static String setTokenAndClean(String username) {
        if (token == null) {
            generateBearerToken(true);
            log.info(token);
        } else {
            String usernameToken = getUsernameDecodeJWToken(token);
            if (collectionVariables != null) {
                token = collectionVariables.get("IdToken").toString();
                usernameToken = getUsernameDecodeJWToken(token);
            }

            if (StringUtils.equalsIgnoreCase(usernameToken, username)) {
                return token;
            }
        }
        log.info(token);
        return token;
    }

    public static String getTokenByUser(String priorUser) {
        List<UserType> listAllCognitoUsers = listAllUsers();
        JsonObject cognitoUsersBundle = getUserDataFromFile(COGNITO_USERS_FILE_LOCATION.getText());
        String[] listAllUsers = configProperties.useCognitoUsers() && !cognitoUsersBundle.isEmpty()
                ? new ArrayList<>(cognitoUsersBundle.keySet()).toArray(new String[0])
                : configProperties.getDefaultUser();

        if (StringUtils.isNotEmpty(priorUser) && !configProperties.getCognitoUsers()) {
            listAllUsers = addToArray(listAllUsers, priorUser);
        }
        sessionUser = "";
        if (listAllCognitoUsers.isEmpty()) {
            throw new SkipException("no users in this pool");
        }
        for (String possibleUser : listAllUsers) {
            for (UserType cognitoUser : listAllCognitoUsers) {
                if (StringUtils.containsIgnoreCase(cognitoUser.username(), possibleUser) &&
                        StringUtils.containsIgnoreCase(possibleUser, priorUser)) {
                    sessionUser = possibleUser;
                    collectionVariables = authenticationGraphQL("login.graphql");

                    break;
                }
            }
            if (StringUtils.isNotEmpty(sessionUser) && !collectionVariables.isEmpty()) {
                break;
            }
        }
        if (StringUtils.isNotEmpty(sessionUser)) {
            return sessionUser;
        } else {
            throw new SkipException("no credentials available");
        }
    }

    /**
     * run a bash command
     *
     * @param command is a key who contains command to run on command_list.json
     * @return bash operation results
     */
    public static String runBashProcess(String command) {
        String params = null;
        String system;
        String mark;
        StringBuilder outputScripts = new StringBuilder();
        createJsonParameters("command_list.json");
        try {
            params = queryVariables.get(command).toString();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (isWindows) {
            system = "cmd.exe";
            mark = "/c";
        } else {
            system = "bash";
            mark = "-c";
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(system, mark, params);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputScripts.append(line).append("\n");
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                log.info("Script was run successfully");
            } else {
                log.info(String.format("Cannot run parameter: %s, please check it out", params));
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return outputScripts.toString();
    }

    /**
     * Dynamo DB connection, needs accessKeyId, secretAccess, endpoint, region on configuration
     * properties.
     *
     * @return amazonDynamoDB global variable
     */
    public AmazonDynamoDB dynamo(boolean isStaticCredential) {
        AWSStaticCredentialsProvider outcomeCredential;
        String AWS_ACCESS_KEY_ID =
                StringUtils.isNotEmpty(System.getenv("AWS_ACCESS_KEY_ID"))
                        ? System.getenv("AWS_ACCESS_KEY_ID")
                        : getAccessKeyId();

        String AWS_SECRET_ACCESS_KEY =
                StringUtils.isNotEmpty(System.getenv("AWS_SECRET_ACCESS_KEY"))
                        ? System.getenv("AWS_SECRET_ACCESS_KEY")
                        : getSecretAccess();

        String AWS_SESSION_TOKEN =
                StringUtils.isNotEmpty(System.getenv("AWS_SESSION_TOKEN"))
                        ? System.getenv("AWS_SESSION_TOKEN")
                        : getSessionToken();

        if (isStaticCredential) {
            BasicSessionCredentials credentials =
                    new BasicSessionCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN);

            outcomeCredential = new AWSStaticCredentialsProvider(credentials);

        } else {
            BasicAWSCredentials credentials =
                    new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
            outcomeCredential = new AWSStaticCredentialsProvider(credentials);
        }

        try {
            amazonDynamoDB =
                    AmazonDynamoDBClientBuilder.standard()
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            configProperties.getEndpoint(), configProperties.getRegion()))
                            .withCredentials(outcomeCredential)
                            .enableEndpointDiscovery()
                            .build();
        } catch (AmazonDynamoDBException e) {
            log.info("Cannot connect with AmazonDynamoDB, reason: " + e);
        }
        return amazonDynamoDB;
    }


    /**
     * clean parameters on expressionAttributeValues
     */
    public void cleanExpressionAttributeValues() {
        expressionAttributeValues.clear();
    }

    /**
     * clean parameters on expressionAttributeNames
     */
    public void cleanExpressionAttributeNames() {
        expressionAttributeNames.clear();
    }

    /**
     * putExpressionAttributeValues contains keys to parametrize scan operations on dynamodb
     * <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html">...</a>
     *
     * @param type  of variable
     * @param key   key to be save on expressionAttributeValues dict.
     * @param value key to be save on expressionAttributeValues dict.
     */
    public void putExpressionAttributeValues(String type, String key, String value) {
        try {
            switch (type.toLowerCase()) {
                case "string":
                    expressionAttributeValues.put(key, new AttributeValue().withS(removeCharacter(value)));
                    break;
                case "number":
                case "integer":
                    expressionAttributeValues.put(key, new AttributeValue().withN(removeCharacter(value)));
                    break;
            }
        } catch (AmazonDynamoDBException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * putExpressionAttributeNames contains keys to parametrize scan operations on dynamodb
     * <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html">...</a>
     *
     * @param key   key to be save on putExpressionAttributeNames dict.
     * @param value key to be save on putExpressionAttributeNames dict.
     */
    public static Map<String, String> putExpressionAttributeNames(String key, String value) {
        try {
            expressionAttributeNames.put(key, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return expressionAttributeNames;
    }

    /**
     * querySpec contains keys to parametrize scan operations on dynamodb
     * <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html">...</a>
     *
     * @param keyConditionExpression, is a query to filter a table results.
     * @param key                     is a id to query on a table.
     * @param value                   is a value to query on a table.
     */
    public QuerySpec querySpec(String keyConditionExpression, String key, String value) {
        try {
            spec =
                    new QuerySpec()
                            .withKeyConditionExpression(keyConditionExpression)
                            .withValueMap(new ValueMap().withString(key, value));
        } catch (AmazonDynamoDBException e) {
            log.info("Filter expression is invalid: " + e);
        }
        return spec;
    }

    /**
     * getScanRequest prepare filters to scan.
     *
     * @param table,               table that we do a query.
     * @param withFilterExpression is a filter to be use on query
     * @return ScanRequest
     */
    private ScanRequest getScanRequest(String table, String withFilterExpression) {
        if (expressionAttributeNames.isEmpty()) {
            return new ScanRequest()
                    .withTableName(table)
                    .withFilterExpression(withFilterExpression)
                    .withExpressionAttributeValues(expressionAttributeValues);
        } else {
            return new ScanRequest()
                    .withTableName(table)
                    .withFilterExpression(withFilterExpression)
                    .withExpressionAttributeNames(expressionAttributeNames)
                    .withExpressionAttributeValues(expressionAttributeValues);
        }
    }

    /**
     * execute query on dynamo db table.
     *
     * @param table,               table that we do a query.
     * @param withFilterExpression is a filter to be use on query
     * @return result is query results.
     */
    public ScanResult ScanAction(String table, String withFilterExpression) {
        try {
            ScanRequest scanRequest = getScanRequest(table, withFilterExpression);
            result = amazonDynamoDB.scan(scanRequest);
        } catch (AmazonDynamoDBException e) {
            log.info("Filter expression is invalid: " + e);
            Assert.fail(e.toString());
            cleanExpressionAttributeValues();
            cleanExpressionAttributeNames();
        }
        cleanExpressionAttributeValues();
        cleanExpressionAttributeNames();
        return result;
    }

    /**
     * create a dynamodb client
     *
     * @return DynamoDB is query results.
     */
    public DynamoDB dynamoDBClient() {

        try {
            dynamoDB = new DynamoDB(amazonDynamoDB);
        } catch (AmazonDynamoDBException e) {
            log.info(e.toString());
        }
        return dynamoDB;
    }

    /**
     * get table data from dynamodb client
     *
     * @return dynamoTable is query results.
     */
    public Table dynamoTable(String Table) {

        try {
            dynamoTable = dynamoDB.getTable(Table);
        } catch (AmazonDynamoDBException e) {
            log.info(e.toString());
        }
        return dynamoTable;
    }

    /**
     * execute a query on dynamodb client
     *
     * @param table                  on that we perform query.
     * @param keyConditionExpression is query conditions.
     * @return items as results of a query.
     */
    public ItemCollection performQuery(String table, QuerySpec keyConditionExpression) {
        try {
            items = dynamoTable(table).query(keyConditionExpression);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return items;
    }

    public static Response postMethodGraphQLGetList(String body) {
        responseList = null;
        setDefaultHeaders();
        generateBearerToken(true);
        builder.addHeader("lang", language);
        setBodyGraphql(body);
        try {
            builder.setBaseUri(setBaseUri);
            request = RestAssured.given().spec(builder.build());
            responseList = request.post(new URI(graphqlEndpoint));
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        return responseList;
    }

    /**
     * Get response from API
     *
     * @return API response
     */
    public static ResponseOptions<Response> getMiddlewareCredentials() {
        try {
            builderMW = createRequestSpecBuilder();
            String environment = getEnvironment().toUpperCase();
            String authenticationFile = getAuthenticationFile(environment);

            builderMW.setBaseUri(configProperties.getHostMiddleWare())
                    .setBody(generateBodyFromResource(authenticationFile))
                    .setContentType(ContentType.TEXT)
                    .setAccept(ContentType.JSON);

            request = RestAssured.given().spec(builderMW.build());
            response = request.post(new URI(configProperties.getMiddlewareEndpoint() + "Login"));

            log.info(response.getBody().prettyPrint());

            validateResponse(response);

            return response;
        } catch (URISyntaxException e) {
            log.error("Error creating URI: " + e.getMessage());
            throw new RuntimeException("Failed to create URI for middleware endpoint", e);
        }
    }

    private static RequestSpecBuilder createRequestSpecBuilder() {
        if (builderMW == null) {
            builderMW = new RequestSpecBuilder();
            builderMW.addHeader("Content-Type", "application/json");
            builderMW.addHeader("Accept", "*/*");

            RestAssuredConfig config = RestAssured.config().httpClient(
                    HttpClientConfig.httpClientConfig()
                            .setParam("http.socket.timeout", 5000)
                            .setParam("http.connection.timeout", 5000)
            );
            builderMW.setConfig(config);
        }


        return builderMW;
    }

    private static String getAuthenticationFile(String environment) {
        switch (environment) {
            case "EVT-DEV":
            case "EVT-DEV-PRV":
                return "authenticationMiddleware.json";
            default:
                return "authenticationMiddlewareCERT.json";
        }
    }

    private static void validateResponse(ResponseOptions<Response> response) {
        if (response == null) {
            throw new RuntimeException("No response received from the server");
        }

        if (StringUtils.containsIgnoreCase(response.getBody().toString(), "Provide a valid Auth token")) {
            throw new SkipException("Invalid Credentials");
        }
    }


    public void matchesJsonSchemaValidator(ResponseOptions<Response> response, String responsePath) {
        JsonSchemaFactory jsonSchemaFactory =
                JsonSchemaFactory.newBuilder()
                        .setValidationConfiguration(
                                ValidationConfiguration.newBuilder()
                                        .setDefaultVersion(SchemaVersion.DRAFTV4)
                                        .freeze())
                        .freeze();
        response
                .thenReturn()
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath(responsePath).using(jsonSchemaFactory));
    }

    public static String getUsername(List<UserType> validUsers) {
        if (!validUsers.isEmpty()) {
            username = validUsers.get(0).username();
            log.info(validUsers.get(0).username());
        }
        return username;
    }

    public static ListUsersResponse paginationCognitoAWS(CognitoIdentityProviderClient cognitoClient,
                                                         String userPoolId, String filter,
                                                         int limit) {
        ListUsersResponse response;
        String paginationToken = null;
        do {
            ListUsersRequest usersRequest =
                    ListUsersRequest.builder().userPoolId(userPoolId)
                            .filter(filter).limit(limit).paginationToken(paginationToken)
                            .build();

            response = cognitoClient.listUsers(usersRequest);

            response.users().forEach(user -> {
                log.info("Username: " + user.username());
            });

            paginationToken = response.paginationToken();
        } while (paginationToken != null);

        return response;
    }

    public static AttributeType createAttributeCognito(String attributeName, String attributeValue) {
        AttributeType attributeCustom = AttributeType.builder()
                .name(attributeName)
                .value(attributeValue)
                .build();

        log.info("Name: " + attributeCustom.name());
        log.info("Value: " + attributeCustom.value());

        return attributeCustom;
    }

    public static String buildFilterByAttribute(AttributeType attributeCustom) {
        String formattedString = "'%s' ^= '%s'";
        return String.format(formattedString, attributeCustom.name(), attributeCustom.value());
    }

    public static List<UserType> getValidUserByStatus(ListUsersResponse response, String status) {
        List<UserType> validUsers = new ArrayList<>();
        response.users().forEach(user -> {
            if (user.enabled() && StringUtils.equalsAnyIgnoreCase(user.userStatus().toString(), status)) {
                validUsers.add(user);
            }
            log.info("Username: " + user.username());
            user.attributes().forEach(attribute -> {
                log.info("Attribute: " + attribute.name() + ", Value: " + attribute.value());
            });
        });

        return validUsers;
    }

    public static List<AttributeType> getAttributeByUsername(ListUsersResponse response,
                                                             String username) {
        List<AttributeType> validAttribute = new ArrayList<>();

        response.users().forEach(user -> {
            log.info("Username: " + user.username());
            if (StringUtils.equalsIgnoreCase(user.username(), username)) {
                user.attributes().forEach(attribute -> {
                    validAttribute.add(attribute);
                    System.out.println("Attribute: " + attribute.name() + ", Value: " + attribute.value());
                });
            }
        });

        return validAttribute;
    }

    public static String getAttributeValueByAttributeName(List<AttributeType> validAttribute,
                                                          String attributeName) {
        String value = null;
        if (!validAttribute.isEmpty()) {
            if (StringUtils.equalsIgnoreCase(validAttribute.get(0).name(), attributeName)) {
                value = validAttribute.get(0).value();
                log.info(String.format("'%s': ", attributeName) + value);
            }
        }
        return value;
    }


    public static int getSizeResponseGraphql(String path) {
        int size;
        try {
            size = 0;
            if (response != null) {
                ArrayList<Object> list =
                        new ArrayList<>(response.getBody().jsonPath().get(path));
                size = list.size();
                Log.info("Total Item Graphql Response:" + size);
            }
        } catch (NullPointerException e) {
            log.error("Path is invalid");
            throw new SkipException("Path is invalid " + e.getMessage());
        }
        return size;
    }
}
