package utils;

import static config.AbstractAPI.*;
import static config.RestAssuredExtension.getUserData;
import static config.RestAssuredPropertiesConfig.getMiddlewareRunDirect;
import static enums.FilesPath.COGNITO_USERS_FILE_LOCATION;
import static utils.CognitoUserHandler.getUserBundleData;
import static utils.CognitoUserHandler.saveInJsonFile;
import static utils.CognitoUserHandler.setDefaultUserWithAttributes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.RestAssuredExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;

public class  UserDataUtils {

  private static final Logger log = Logger.getLogger(UserDataUtils.class);
  static final Map<String, AttributeCondition> userConditions =
    initializeUserConditions();

  private static Map<String, AttributeCondition> initializeUserConditions() {
    Map<String, AttributeCondition> conditions = new HashMap<>();
    conditions.put("Admin", new AttributeCondition(Collections.singletonMap("isAdmin", true)));
    conditions.put("NoAdmin", new AttributeCondition(Collections.singletonMap("isAdmin", false)));
    conditions.put("TrxPending", new AttributeCondition(Collections.singletonMap("hasTrxPend", true)));
    conditions.put("NoTrxPending", new AttributeCondition(Collections.singletonMap("hasTrxPend", false)));
    conditions.put("TrxProcessed", new AttributeCondition(Collections.singletonMap("hasTrxProcessed", true)));
    conditions.put("NoTrxProcessed", new AttributeCondition(Collections.singletonMap("hasTrxProcessed", false)));
    conditions.put("isNotBlockedTrue", new AttributeCondition(Collections.singletonMap("isNotBlocked", true)));
    conditions.put("isNotBlockedFalse", new AttributeCondition(Collections.singletonMap("isNotBlocked", false)));
    conditions.put("isCardActiveTrue", new AttributeCondition(Collections.singletonMap("isCardActive", true)));
    conditions.put("isCardActiveFalse", new AttributeCondition(Collections.singletonMap("isCardActive", false)));
    conditions.put("isCreditCardExpiredTrue", new AttributeCondition(Collections.singletonMap("isCreditCardExpired", true)));

    Map<String, Boolean> adminAndCardNotExpired = new HashMap<>();
    adminAndCardNotExpired.put("isAdmin", true);
    adminAndCardNotExpired.put("isCreditCardExpired", false);
    conditions.put("AdminAndIsCreditCardExpiredFalse", new AttributeCondition(adminAndCardNotExpired));
    return conditions;
  }

  public static JsonObject getUserDataFromFile(String cognitoUsers) {
    JsonObject usersBundle = null;
    try {
      String usersPath = new String(Files.readAllBytes(Paths.get(cognitoUsers)));
      usersBundle = JsonParser.parseString(usersPath).getAsJsonObject();
    } catch (IOException e) {
      log.error("IOException occurred while reading the file: {}" + e.getMessage());
      Assert.fail("File reading error: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error occurred while parsing JSON: {}" + e.getMessage());
      Assert.fail("JSON parsing error: " + e.getMessage());
    }
    return usersBundle;
  }

  public static JsonObject getCurrentUserData() {
      JsonObject usersBundle;
      if(StringUtils.containsIgnoreCase(getMiddlewareRunDirect(), "true")){
          usersBundle= getUserData();
      }else{
          usersBundle = RestAssuredExtension.getUserBundleData();
      }

    JsonObject userData = null;

    try {
      for (Map.Entry<String, JsonElement> entry : usersBundle.entrySet()) {
        String key = entry.getKey();
        if (StringUtils.containsIgnoreCase(key, RestAssuredExtension.sessionUser)) {
          userData = entry.getValue().getAsJsonObject();
          break;
        }
      }
      if (!usersBundle.isEmpty() && userData == null) {
        userData = usersBundle.entrySet().iterator().next().getValue().getAsJsonObject();
      }

      if (userData == null) {
        throw new SkipException("Credentials are empty for session user: " + RestAssuredExtension.sessionUser);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving user data: " + e.getMessage(), e);
    }

    return userData;
  }


  public static JsonObject getCurrentUserDataByUsername(String username) {
    JsonObject usersBundle = getUserData();
    JsonObject userData;
    try {
      userData = usersBundle.getAsJsonObject(username);
    } catch (Exception e) {
      throw new SkipException("Credentials are empty for username: " + e.getMessage());
    }
    return userData;
  }

  public static String getUserByCondition(String conditionKey) {
    AttributeCondition condition = userConditions.get(conditionKey);

    if (condition != null) {
      Map<String, Boolean> attributes = condition.getAttributes();
      String user = null;

      for (Map.Entry<String, Boolean> entry : attributes.entrySet()) {
        user = rest.getDefaultUserWithAttributes(entry.getKey(), entry.getValue());
        if (user == null) {
          log.warn("No user found for attribute: " + entry.getKey() + " with value: " + entry.getValue());
          break;
        }
      }

      return user;
    }

    log.warn("No valid user condition found for: " + conditionKey);
    return null;
  }


  public static String updateUserPermissionsAndReturn(String userType) {
    String username = getUserFromBundle(userType);

    if (StringUtils.equalsAny(userType, "Admin")) {
      log.info("Granting 'Admin' permissions to the user: " + username);
      updateUserPermissions(username, "admins-group", "isAdmin", true);
      return rest.getDefaultUserWithAttributes("isAdmin", true);
    }

    if (StringUtils.equalsAny(userType, "NoAdmin")) {
      log.info("Removing 'Admin' permissions from the user: " + username);
      updateUserPermissions(username, "admins-group", "isAdmin", false);
      return rest.getDefaultUserWithAttributes("isAdmin", false);
    }

    log.warn("No valid user condition found for: " + userType);
    return userType;
  }

  private static String getUserFromBundle(String userType) {
    String defaultUsername = getUserBundleData().entrySet().stream().findFirst().map(Map.Entry::getKey).orElse(null);

    if (StringUtils.equalsAny(userType, "Admin")) {
      return getUserBundleData().has("Yuliet2023") ? "Yuliet2023" : defaultUsername;
    }

    if (StringUtils.equalsAny(userType, "NoAdmin")) {
      return getUserBundleData().has("Edgardo2023") ? "Edgardo2023" : defaultUsername;
    }

    return defaultUsername;
  }

  private static void updateUserPermissions(String username, String groupName, String attribute, boolean isAdmin) {
    if (isAdmin) {
      addPermissionCognitoUser(groupName, username);
    } else {
      removePermissionCognitoUser(groupName, username);
    }

    updatePermissionsFile(username, attribute, Boolean.toString(isAdmin));
  }

  public static void updatePermissionsFile(String username, String attribute, String value) {
    log.info("Updating permissions file for user: " + username + ", setting " + attribute + " to " + value);

    JsonObject cognitoUsersResult = setDefaultUserWithAttributes(attribute, true);
    JsonElement jsonElement = cognitoUsersResult.get(username);

    if (jsonElement != null && jsonElement.isJsonObject()) {
      jsonElement.getAsJsonObject().addProperty(attribute, Boolean.parseBoolean(value));
    }

    saveInJsonFile(COGNITO_USERS_FILE_LOCATION.getText(), cognitoUsersResult);
  }

}
