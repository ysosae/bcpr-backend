package test;

import static config.CognitoAWS.getSubIdByUsernameIntoCognito;
import static config.CognitoAWS.isPresentUsersCognito;
import static config.RestAssuredExtension.configProperties;
import static config.RestAssuredPropertiesConfig.awsBasicCredentials;
import static enums.FilesPath.COGNITO_USERS_FILE_LOCATION;
import static utils.CognitoUserHandler.retrieveUserListFromCognitoMatches;
import static utils.CognitoUserHandler.saveInJsonFile;

import com.amazonaws.auth.BasicSessionCredentials;
import com.google.gson.JsonObject;
import config.RestAssuredExtension;
import config.RestAssuredPropertiesConfig;
import config.ServicesClientAWS;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public class CognitoTest {
  private static final Logger log = Logger.getLogger(CognitoTest.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static ServicesClientAWS servicesClientAWS = new ServicesClientAWS();
  public static CognitoIdentityProviderClient cognitoIdentityProviderClient =
    servicesClientAWS.getCognitoIdentityProviderClient();
  public static String username = null;

  @Test
  public void handleCognitoUsers() {

    String userPoolId = configProperties.getUserPoolId();

    String filter = "username = \"Automation2023\"";

    ListUsersRequest usersRequest =
      ListUsersRequest.builder().userPoolId(userPoolId).filter(filter).build();

    ListUsersResponse response = cognitoIdentityProviderClient.listUsers(usersRequest);

    Optional<UserType> user = response.users().stream().findFirst();
    log.info(user);

    try {
      AdminGetUserRequest getUserRequest =
        AdminGetUserRequest.builder().userPoolId(userPoolId).username("Patria2023").build();

      cognitoIdentityProviderClient.adminGetUser(getUserRequest);

      AdminDeleteUserRequest adminDeleteUserRequest =
        AdminDeleteUserRequest.builder().userPoolId(userPoolId).username("Patria2023").build();

      cognitoIdentityProviderClient.adminDeleteUser(adminDeleteUserRequest);
      log.info("User still exists.");
    } catch (UserNotFoundException e) {
      log.error("User has been successfully deleted.");
    }
  }
  @Ignore
  @Test
  public void scanCognito() {
    String AWS_ACCESS_KEY_ID =
      StringUtils.isNotEmpty(System.getenv("AWS_ACCESS_KEY_ID"))
        ? System.getenv("AWS_ACCESS_KEY_ID")
        : RestAssuredPropertiesConfig.getAccessKeyId();

    String AWS_SECRET_ACCESS_KEY =
      StringUtils.isNotEmpty(System.getenv("AWS_SECRET_ACCESS_KEY"))
        ? System.getenv("AWS_SECRET_ACCESS_KEY")
        : RestAssuredPropertiesConfig.getSecretAccess();

    String AWS_SESSION_TOKEN =
      StringUtils.isNotEmpty(System.getenv("AWS_SESSION_TOKEN"))
        ? System.getenv("AWS_SESSION_TOKEN")
        : RestAssuredPropertiesConfig.getSessionToken();

    String userPoolId = configProperties.getUserPoolId();

    BasicSessionCredentials credentials =
      new BasicSessionCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN);

    AwsCredentials awsCredentials =
      AwsSessionCredentials.create(
        credentials.getAWSAccessKeyId(),
        credentials.getAWSSecretKey(),
        credentials.getSessionToken());

    CognitoIdentityProviderClient identityProviderClient =
      CognitoIdentityProviderClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .build();

    log.info("---------LIST USER--------");
    listAllUsers(identityProviderClient, userPoolId);
    log.info("------------FILTER BY E-MAIL AND LIST THIS USER-----------");
    listUsersFilter( "us-east-1_7Umhgx87L");
    log.info("------------LIST ALL ATTRIBUTE BY FILTER USER -----------");
    listUsersFilterByAttribute( "us-east-1_7Umhgx87L");
    log.info("------------FILTER BY E-MAIL AND LIST THIS USER-----------");
    log.info("----------DELETED USER WITH USERNAME--------");

    if (StringUtils.equalsIgnoreCase(RestAssuredPropertiesConfig.getEnvironment(), "EVT-TST")) {
      username =
        filterUsersByEmails(identityProviderClient, userPoolId, "phone_number", "+17876548795");
    } else if (StringUtils.equalsIgnoreCase(RestAssuredPropertiesConfig.getEnvironment(),
      "EVT-DEV")) {
      username =
        filterUsersByEmails(identityProviderClient, userPoolId, "phone_number", "+17876548795");
    } else if (StringUtils.equalsIgnoreCase(RestAssuredPropertiesConfig.getEnvironment(),
      "EVT-DEV-IMP")) {
      username =
        filterUsersByEmails(identityProviderClient, userPoolId, "phone_number", "+17862587565");
      String subId =
        filterUsersByEmails(
          identityProviderClient, userPoolId, "sub", "0390f26e-d378-4d3f-b535-cfafd4041bad");
      log.info(subId);

      AttributeType attributeCustom = createAttributeCognito("given_name", "YALITHZA");
      String filter = buildFilterByAttribute(attributeCustom);
      ListUsersResponse response =
        paginationCognitoAWS(userPoolId, filter, 10);
      getValidUserByStatus(response, "CONFIRMED");
      List<AttributeType> validAttribute = getAttributeByUsername(response, "Llovet2023");
      String subID = getAttributeValueByAttributeName(validAttribute, "sub");
      log.info("SUB: " + subID);
    }
  }

  public static void listAllUsers(CognitoIdentityProviderClient cognitoClient, String userPoolId) {
    try {
      ListUsersRequest usersRequest = ListUsersRequest.builder().userPoolId(userPoolId).build();

      ListUsersResponse response = cognitoClient.listUsers(usersRequest);

      List<UserType> validUsers = new ArrayList<>();
      response
        .users()
        .forEach(
          user -> {
            if (user.enabled()
              && StringUtils.equalsAnyIgnoreCase(user.userStatus().toString(), "CONFIRMED")) {
              validUsers.add(user);
            }
            System.out.println(
              "User "
                + user.username()
                + " Enabled "
                + user.enabled()
                + " Status "
                + user.userStatus()
                + " Created "
                + user.userCreateDate());
          });

      log.info(validUsers.size());

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  public static String filterUsersByEmails(
    CognitoIdentityProviderClient cognitoClient,
    String userPoolId,
    String attributeName,
    String attributeValue) {
    String username = null;
    String subID;
    try {
      List<UserType> validUsers = new ArrayList<>();
      List<AttributeType> validAttribute = new ArrayList<>();

      int limit = 10;
      String paginationToken = null;

      do {
        AttributeType attributeCustom =
          AttributeType.builder().name(attributeName).value(attributeValue).build();

        log.info("Name: " + attributeCustom.name());
        log.info("Value: " + attributeCustom.value());

        String formattedString = "'%s' ^= '%s'";
        String filter =
          String.format(formattedString, attributeCustom.name(), attributeCustom.value());

        ListUsersRequest usersRequest =
          ListUsersRequest.builder()
            .userPoolId(userPoolId)
            .filter(filter)
            .limit(limit)
            .paginationToken(paginationToken)
            .build();

        ListUsersResponse response = cognitoClient.listUsers(usersRequest);

        response
          .users()
          .forEach(
            user -> {
              if (user.enabled()
                && StringUtils.equalsAnyIgnoreCase(
                user.userStatus().toString(), "CONFIRMED")) {
                validUsers.add(user);
              }
              log.info("Username: " + user.username());
              if (StringUtils.equalsIgnoreCase(user.username(), "facundovillalba-uat")) {
                user.attributes()
                  .forEach(
                    attribute -> {
                      validAttribute.add(attribute);
                      log.info(
                        "Attribute: "
                          + attribute.name()
                          + ", Value: "
                          + attribute.value());
                    });
              }
            });
        paginationToken = response.paginationToken();
      } while (paginationToken != null);

      if (!validUsers.isEmpty()) {
        username = validUsers.get(0).username();
        log.info(validUsers.get(0).username());
      }
      if (!validAttribute.isEmpty()) {
        if (StringUtils.equalsIgnoreCase(validAttribute.get(0).name(), "sub")) {
          subID = validAttribute.get(0).value();
          log.info("Username: " + validUsers.get(0).username() + ", Sub: " + subID);
        }
      }

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return username;
  }

  public static ListUsersResponse paginationCognitoAWS(String userPoolId, String filter, int limit) {
    ListUsersResponse response;
    String paginationToken = null;
    do {

      ListUsersRequest usersRequest =
        ListUsersRequest.builder()
          .userPoolId(userPoolId)
          .filter(filter)
          .limit(limit)
          .paginationToken(paginationToken)
          .build();

      response = cognitoIdentityProviderClient.listUsers(usersRequest);
      paginationToken = response.paginationToken();

    } while (paginationToken != null);

    return response;
  }

  public static AttributeType createAttributeCognito(String attributeName, String attributeValue) {
    AttributeType attributeCustom =
      AttributeType.builder().name(attributeName).value(attributeValue).build();

    log.info("Name: " + attributeCustom.name());
    log.info("Value: " + attributeCustom.value());

    return attributeCustom;
  }

  public static String buildFilterByAttribute(AttributeType attributeCustom) {
    String formattedString = "'%s' ^= '%s'";
    return String.format(formattedString, attributeCustom.name(), attributeCustom.value());
  }

  public static void getValidUserByStatus(ListUsersResponse response, String status) {
    List<UserType> validUsers = new ArrayList<>();

    response
      .users()
      .forEach(
        user -> {
          if (user.enabled()
            && StringUtils.equalsAnyIgnoreCase(user.userStatus().toString(), status)) {
            validUsers.add(user);
          }
          log.info("Username: " + user.username());
          user.attributes()
            .forEach(
              attribute -> log.info(
                "Attribute: " + attribute.name() + ", Value: " + attribute.value()));
        });
       log.info(validUsers);
  }

  public static List<AttributeType> getAttributeByUsername(
    ListUsersResponse response, String username) {
    List<AttributeType> validAttribute = new ArrayList<>();

    response
      .users()
      .forEach(
        user -> {
          log.info("Username: " + user.username());
          if (StringUtils.equalsIgnoreCase(user.username(), username)) {
            user.attributes()
              .forEach(
                attribute -> {
                  validAttribute.add(attribute);
                  log.info(
                    "Attribute: " + attribute.name() + ", Value: " + attribute.value());
                });
          }
        });

    return validAttribute;
  }

  public static String getAttributeValueByAttributeName(
    List<AttributeType> validAttribute, String attributeName) {
    String value = null;
    if (!validAttribute.isEmpty()) {
      if (StringUtils.equalsIgnoreCase(validAttribute.get(0).name(), attributeName)) {
        value = validAttribute.get(0).value();
        log.info(String.format("'%s': ", attributeName) + value);
      }
    }
    return value;
  }




  public static void listUsersFilter(String userPoolId) {

    try {
      String filter = "username = \"Yuliet2023\"";

      ListUsersRequest usersRequest =
        ListUsersRequest.builder().userPoolId(userPoolId).filter(filter).build();

      ListUsersResponse response = cognitoIdentityProviderClient.listUsers(usersRequest);
      response
        .users()
        .forEach(
          user -> log.info(
            "User with filter applied "
              + user.username()
              + " Enabled "
              + user.enabled()
              + " Status "
              + user.userStatus()
              + " Created "
              + user.userCreateDate()));

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  public static void listUsersFilterByAttribute(
     String userPoolId) {
    try {
      String filter = "username = \"facundovillalba-uat\"";
      ListUsersRequest usersRequest =
        ListUsersRequest.builder().userPoolId(userPoolId).filter(filter).build();

      ListUsersResponse response = cognitoIdentityProviderClient.listUsers(usersRequest);
      List<AttributeType> attributes = response.users().get(0).attributes();

      attributes.forEach(
        attribute -> log.info(attribute.name() + ": " + attribute.value()));

    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  @Test
  public void statusUserCognito() {
    String userPoolId = configProperties.getUserPoolId();

    try {
      ListUsersRequest usersRequest = ListUsersRequest.builder().userPoolId(userPoolId).build();
      ListUsersResponse responseListUser = cognitoIdentityProviderClient.listUsers(usersRequest);
      log.info(responseListUser);

      AdminEnableUserRequest enableUserRequest = AdminEnableUserRequest
        .builder().userPoolId(userPoolId)
        .username("Yuliet2023")
        .build();

      AdminEnableUserResponse enableUserResult =
        cognitoIdentityProviderClient.adminEnableUser(enableUserRequest);

      if (enableUserResult.sdkHttpResponse().statusCode() == 200) {
        log.info("User has been enabled successfully.");
      } else {
        log.info("Failed to enable the user.");
      }
    } catch (CognitoIdentityProviderException e) {
      log.error(e.awsErrorDetails().errorMessage());
    }
  }

  @Test
  public void testRetrieveUserList() {
    List<UserType> listAllUsers = listAllUsers();
    for (UserType user : listAllUsers) {
      log.info(
        "User with filter applied "
          + user.username()
          + " Enabled "
          + user.enabled()
          + " Status "
          + user.userStatus()
          + " Created "
          + user.userCreateDate());
    }
  }

  @Test
  public void AddPermissionCognitoUser() {
    try {
      AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
        .userPoolId(configProperties.getUserPoolId())
        .groupName("admins-group")
        .username("Candido2023")
        .build();

      cognitoIdentityProviderClient.adminAddUserToGroup(addUserToGroupRequest);

    } catch (CognitoIdentityProviderException e) {
      log.error(e.getMessage());
      throw new SkipException("Cognito client didn't contains functions available" + e.getMessage());
    }

  }

  @Test
  public void listPermissionCognitoByUser() {
    try {
      String userPoolId = configProperties.getUserPoolId();
      String username = "Candido2023";
      String expectedGroupName = "admins-group";

      AdminListGroupsForUserRequest request = AdminListGroupsForUserRequest.builder()
        .userPoolId(userPoolId)
        .username(username)
        .build();

      AdminListGroupsForUserResponse response = cognitoIdentityProviderClient.adminListGroupsForUser(request);

      GroupType groupType = response.groups().stream()
        .filter(group -> StringUtils.equalsIgnoreCase(group.groupName(), expectedGroupName))
        .findFirst()
        .orElse(null);

      assert groupType != null;
      Assert.assertEquals("Group name does not match", expectedGroupName, groupType.groupName());
      log.info("Group name: " + groupType.groupName());

    } catch (CognitoIdentityProviderException e) {
      throw new SkipException("Cognito client functions are not available: " + e.getMessage());
    }
  }


  public static List<UserType> listAllUsers() {
    List<UserType> validUsers = new ArrayList<>();
    String userPoolId = configProperties.getUserPoolId();

    try (CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build()) {

      ListUsersRequest usersRequest = ListUsersRequest.builder()
        .userPoolId(userPoolId)
        .build();

      ListUsersResponse response = identityProviderClient.listUsers(usersRequest);

      validUsers = response.users().stream()
        .filter(user -> user.enabled() &&
          StringUtils.equalsIgnoreCase(user.userStatus().toString(), "CONFIRMED"))
        .collect(Collectors.toList());

      log.info("Number of valid users: " + validUsers.size());

    } catch (CognitoIdentityProviderException e) {
      log.error("Error listing users: " + e.awsErrorDetails().errorMessage());
    }

    return validUsers;
  }


  @Test
  public void getSubIdIntoCognitoByUser() {
   String username= "Patria2023";
    String sub = getSubIdByUsernameIntoCognito(username);
    Assert.assertEquals(sub, "55b58d3f-7f17-456f-b718-af227c3c2861");
  }


  @Test
  public void retrieveUserDataLocal() {
    JsonObject cognitoUsersResult = retrieveUserListFromCognitoMatches();
    saveInJsonFile(COGNITO_USERS_FILE_LOCATION.getText(), cognitoUsersResult);
  }

  @Test
  public void validatePresentUserIntoCognito() {
   Assert.assertTrue(isPresentUsersCognito("perficientclienttest@gmail.com"));
  }
}
