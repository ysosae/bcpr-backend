package utils;
import static config.AbstractAPI.retrieveValidUsersByFilter;
import static config.AbstractAPI.setPasswordBase;
import static config.DynamoDbAWS.removeExistingLoginAttemptsDynamoDB;
import static config.RestAssuredExtension.authenticationGraphQL;
import static config.RestAssuredExtension.builder;
import static config.RestAssuredExtension.collectionVariables;
import static config.RestAssuredExtension.configProperties;
import static config.RestAssuredExtension.getRoleDecodeJWToken;
import static config.RestAssuredExtension.getUserData;
import static config.RestAssuredExtension.request;
import static config.RestAssuredExtension.response;
import static config.RestAssuredExtension.sessionUser;
import static config.RestAssuredExtension.setBodyGraphql;
import static config.RestAssuredExtension.setProxyEnv;
import static config.RestAssuredExtension.userCognitoData;
import static config.RestAssuredPropertiesConfig.getEnvironment;
import static enums.FilesPath.COGNITO_USERS_FILE_LOCATION;
import static utils.UserDataUtils.getUserDataFromFile;

import com.google.gson.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public class CognitoUserHandler {
  private static final Logger log = Logger.getLogger(CognitoUserHandler.class);
  public static JsonObject usersBundle = new JsonObject();

  public static JsonObject retrieveUserListFromCognitoMatches() {
    JsonObject usersBundle = getUserData();
    JsonObject newUsersBundle = new JsonObject();

    if (!usersBundle.entrySet().isEmpty()) {
      usersBundle.entrySet().forEach(entry -> {
        JsonObject keyValue = (JsonObject) entry.getValue();
        String email = keyValue.get("email").getAsString();

        List<UserType> validUsers = retrieveValidUsersByFilter("email", email);
        List<UserType> validPerficient = retrieveValidUsersByFilter("email", "perficientclienttest@gmail.com");
        validUsers.addAll(validPerficient);

        for (UserType user : validUsers) {
          if (isValidUser(user)) {
            List<AttributeType> attributes = user.attributes();
            AtomicBoolean nameMatch = new AtomicBoolean(false);
            AtomicBoolean lastnameMatch = new AtomicBoolean(false);
            AtomicBoolean phoneMatch = new AtomicBoolean(false);
            AtomicBoolean customerIdMatch = new AtomicBoolean(false);

            attributes.forEach(attribute -> setMatchFlags(attribute, keyValue, nameMatch, lastnameMatch, phoneMatch, customerIdMatch));

            if (nameMatch.get() && lastnameMatch.get() && phoneMatch.get() && customerIdMatch.get() && !newUsersBundle.has(user.username())) {
              handleUserMatch(user, keyValue, newUsersBundle, attributes);
            }
          }
        }
      });
    }

    if (newUsersBundle.entrySet().isEmpty()) {
      log.debug("No matches user with cognito environment: " + getEnvironment());
    }
    return newUsersBundle;
  }

  private static boolean isValidUser(UserType user) {
    return user.enabled() && StringUtils.equalsIgnoreCase(user.userStatus().toString(), "CONFIRMED") && user.hasAttributes();
  }

  public static JsonObject getUserBundleData() {
    usersBundle = configProperties.useCognitoUsers()
      ? getUserDataFromFile(COGNITO_USERS_FILE_LOCATION.getText())
      : getUserData();
    return usersBundle;
  }


  private static void setMatchFlags(AttributeType attribute, JsonObject keyValue, AtomicBoolean nameMatch, AtomicBoolean lastnameMatch, AtomicBoolean phoneMatch, AtomicBoolean customerIdMatch) {
    switch (attribute.name()) {
      case "given_name":
        if (keyValue.has("name")) {
          String name = keyValue.get("name").getAsString();
          nameMatch.set(StringUtils.containsIgnoreCase(attribute.value(), name.trim()));
        }
        break;
      case "family_name":
        if (keyValue.has("lastName")) {
          String lastName = keyValue.get("lastName").getAsString();
          lastnameMatch.set(StringUtils.containsIgnoreCase(attribute.value(), lastName.trim()));
        }
        break;
      case "phone_number":
        if (keyValue.has("phone")) {
          String phone = keyValue.get("phone").getAsString();
          phoneMatch.set(StringUtils.containsIgnoreCase(attribute.value(), phone.trim()));
        }
        break;
      case "custom:ssn":
        if (keyValue.has("customerId")) {
          String customerId = keyValue.get("customerId").getAsString();
          customerIdMatch.set(StringUtils.containsIgnoreCase(attribute.value(), customerId.trim()));
        }
        break;
    }
  }

  private static void handleUserMatch(UserType user, JsonObject keyValue, JsonObject newUsersBundle, List<AttributeType> attributes) {
    collectionVariables = loginUserInEnv(user);
    if (collectionVariables.isEmpty()) {
      log.info(sessionUser + ": invalid credentials, unable to login, trying with a new one...");
      removeExistingLoginAttemptsDynamoDB(user.username());
    } else {
      setAdditionalUserAttributes(user, keyValue, attributes);
      newUsersBundle.add(user.username(), keyValue);
    }
  }

  private static void setAdditionalUserAttributes(UserType user, JsonObject keyValue, List<AttributeType> attributes) {
    setIsAdmin(keyValue);
    setHasTrxPend(keyValue);
    setCardStatus(keyValue);
    setHasTrxProcessed(keyValue);
    setLoginData(user, keyValue);
    setAttributeSub(attributes, "sub", keyValue);
    setHasListWallet(keyValue);
    setHasFindRecentPayments(keyValue);
  }

  public static JsonObject setDefaultUserWithAttributes(String attribute, boolean value) {
    JsonObject usersBundle = getUserBundleData();
    usersBundle.entrySet().forEach(entry -> {
      JsonObject userData = (JsonObject) entry.getValue();
      if (!userData.has(attribute) || !userData.get(attribute).getAsBoolean()) {
        userData.addProperty(attribute, value);
      }
    });
    return usersBundle;
  }

  public static JsonObject loginUserInEnv(UserType user) {
    userCognitoData.addProperty("USERNAME", user.username());
    userCognitoData.addProperty("PASSWORD", setPasswordBase());
    userCognitoData.addProperty("ClientId", configProperties.getClientId());
    return authenticationGraphQL(userCognitoData, "login.graphql");
  }

  public static void setIsAdmin(JsonObject keyValue) {
    String isAdminStr = getRoleDecodeJWToken(collectionVariables.get("IdToken").getAsString());
    boolean isAdmin = StringUtils.containsIgnoreCase(isAdminStr, "Admins");
    keyValue.addProperty("isAdmin", isAdmin);
  }

  public static void setAttributeSub(List<AttributeType> attributes, String attributeName, JsonObject keyValue) {
    attributes.stream()
      .filter(attribute -> attribute.name().equalsIgnoreCase(attributeName))
      .findFirst()
      .ifPresent(attribute -> keyValue.addProperty(attributeName, attribute.value()));
  }

  public static void saveInJsonFile(String path, JsonObject jsonValues) {
    try (PrintWriter out = new PrintWriter(new FileWriter(
      Paths.get("").toAbsolutePath() + "/" + path))) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String jsonString = gson.toJson(jsonValues);
      out.write(jsonString);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public static void setHasTrxPend(JsonObject keyValue) {
    builder = new RequestSpecBuilder();
    try {
      setBodyGraphql("/graphQL/listInProcessTransactions.graphql", keyValue);
            builder.addHeader("Authorization", String.format("Bearer %s", collectionVariables.get("IdToken").toString()));
            builder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv();
            request = RestAssured.given().spec(builder.build());
            response = request.post(new URI(configProperties.getGraphqlEndpoint()));

      boolean hasTrxPend = response.getBody().jsonPath().get("errors") == null
        && response.getBody().jsonPath().get("data.listInProcessTransactions") != null;

      keyValue.addProperty("hasTrxPend", hasTrxPend);
    } catch (URISyntaxException e) {
      log.error("URISyntaxException: " + e.getMessage());
    } catch (Exception e) {
      log.error("Exception: " + e.getMessage());
    }
  }

public static void setHasTrxProcessed(JsonObject keyValue) {
  builder = new RequestSpecBuilder();
    try {
    setBodyGraphql("/graphQL/listTransactionsWithSelectedDate.graphql", keyValue);
           builder.addHeader("Authorization", String.format("Bearer %s", collectionVariables.get("IdToken").toString()));
            builder.setBaseUri(configProperties.getBaseUri());
            setProxyEnv();
            request = RestAssured.given().spec(builder.build());
            response = request.post(new URI(configProperties.getGraphqlEndpoint()));

    boolean hasTrxProcessed = response.getBody().jsonPath().get("errors") == null
      && response.getBody().jsonPath().get("data.listTransactions.transactions[0]") != null;

     keyValue.addProperty("hasTrxProcessed", hasTrxProcessed);

  } catch (URISyntaxException e) {
    log.error("URISyntaxException: " + e.getMessage());
  } catch (Exception e) {
    log.error("Exception: " + e.getMessage());
  }
}

  public static void setHasFindRecentPayments(JsonObject keyValue) {
    builder = new RequestSpecBuilder();
    request= null;
    try {
      setBodyGraphql("/graphQL/findRecentPayments.graphql", keyValue);
      builder.addHeader("Authorization", String.format("Bearer %s", collectionVariables.get("IdToken").toString()));
      builder.setBaseUri(configProperties.getBaseUri());
      setProxyEnv();
      request = RestAssured.given().spec(builder.build());
      response = request.post(new URI(configProperties.getGraphqlEndpoint()));

      boolean hasRecentPayment = response.getBody().jsonPath().get("errors") != null
        && response.getBody().jsonPath().get("data.findRecentPayments[0]") != null
        && StringUtils.containsIgnoreCase(response.getBody().jsonPath().get("errors[0].extensions.code").toString(),
        "404");
      keyValue.addProperty("hasRecentPayment", hasRecentPayment);
    } catch (URISyntaxException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error("Exception: " + e.getMessage());
    }
  }

  public static void setCardStatus(JsonObject keyValue) {
    builder = new RequestSpecBuilder();

    try {
      setBodyGraphql("/graphQL/listCreditsCards.graphql", keyValue);
      builder.addHeader("Authorization", String.format("Bearer %s", collectionVariables.get("IdToken").getAsString()));
      builder.setBaseUri(configProperties.getBaseUri());
      setProxyEnv();
      request = RestAssured.given().spec(builder.build());
      response = request.post(new URI(configProperties.getGraphqlEndpoint()));
      log.info(response.getBody().prettyPrint());
    } catch (URISyntaxException e) {
      log.error(e.getMessage());
    }

    JsonArray cardData = new JsonArray();
    boolean isCardActive = false, isCreditCardExpired = false, isNotBlocked = false;
    int cardsQuantity = 0;

      if (response.getBody().jsonPath().get("errors") == null && response.getBody().jsonPath().get("data.listCreditsCards") != null) {
         List<Map<String, Object>> cardsList = response.getBody().jsonPath().getList("data.listCreditsCards");

      for (Map<String, Object> cardMap : cardsList) {
        JsonObject cardJsonObject = new JsonObject();
        cardMap.forEach((key, value) -> {
          if (value instanceof Boolean) {
            cardJsonObject.addProperty(key, (Boolean) value);
          } else if (value instanceof Number) {
            cardJsonObject.addProperty(key, (Number) value);
          } else if (value instanceof String) {
            cardJsonObject.addProperty(key, (String) value);
          }
        });
        cardData.add(cardJsonObject);

           boolean cardActive = cardJsonObject.get("isActive").getAsBoolean();
        boolean cardExpired = cardJsonObject.get("isCreditCardExpired").getAsBoolean();
        String notBlockedValue = cardJsonObject.get("blockingStatus").getAsString();
        boolean cardNotBlocked = StringUtils.isNotEmpty(notBlockedValue) && StringUtils.equalsIgnoreCase("NoBlocked", notBlockedValue);

        log.info(String.format("Processing card with ID %s: Active=%s, Expired=%s, NotBlocked=%s",
          cardJsonObject.get("id").getAsString(), cardActive, cardExpired, cardNotBlocked));

        if (!isCardActive && cardActive) {
          isCardActive = true;
        }
        if (!isCreditCardExpired && cardExpired) {
          isCreditCardExpired = true;
        }
        if (!isNotBlocked && cardNotBlocked) {
          isNotBlocked = true;
        }
      }

      cardsQuantity = cardData.size();
    }

    keyValue.addProperty("isCardActive", isCardActive);
    keyValue.addProperty("isCreditCardExpired", isCreditCardExpired);
    keyValue.addProperty("cardsQuantity", cardsQuantity);
    keyValue.addProperty("isNotBlocked", isNotBlocked);
    if (!cardData.isEmpty()) {
      keyValue.add("cardData", cardData);
    }
  }



  public static void setLoginData(UserType user, JsonObject keyValue) {
    JsonArray login = new JsonArray();
    login.add(user.username());
    login.add(setPasswordBase());
    keyValue.add("login", login);
    keyValue.add("login_app", login);
  }

  public static void setHasListWallet(JsonObject keyValue) {
    builder = new RequestSpecBuilder();
    request= null;
    try {
      setBodyGraphql("/graphQL/getListWalletAccounts.graphql", keyValue);
      builder.addHeader("Authorization", String.format("Bearer %s", collectionVariables.get("IdToken").toString()));
      builder.setBaseUri(configProperties.getBaseUri());
      setProxyEnv();
      request = RestAssured.given().spec(builder.build());
      response = request.post(new URI(configProperties.getGraphqlEndpoint()));
      boolean hasWallet = response.getBody().jsonPath().get("errors") != null
        && response.getBody().jsonPath().get("data.listWalletAccounts[0]") != null
        && StringUtils.containsIgnoreCase(response.getBody().jsonPath().get("errors[0].extensions.code").toString(),
        "404");

      keyValue.addProperty("hasWallet", hasWallet);

    } catch (URISyntaxException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error("Exception: " + e.getMessage());
    }
  }
}
