package config;

import static config.AbstractAPI.getUsersResponseWithFilter;
import static config.AbstractAPI.retrieveValidUsersByFilter;
import static config.ResourcesAWS.getEnvironment;
import static config.RestAssuredExtension.subId;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public class CognitoAWS {
  private static final Logger log = Logger.getLogger(CognitoAWS.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();

  public static String getSubIdByUsernameIntoCognito(String username) {
    List<UserType> validUsers =
      retrieveValidUsersByFilter("email", "perficientclienttest@gmail.com");
    for (UserType user : validUsers) {
      if (StringUtils.equalsIgnoreCase(user.username(), username)) {
        List<AttributeType> attributes = user.attributes();
        attributes.forEach(attribute -> {
          if (StringUtils.equalsIgnoreCase(attribute.name(), "sub")) {
            subId = attribute.value();
            log.info("############## RESPONSE TEST ###########");
            log.info("The user " + username + " has subId: " + subId);
            log.info("############## RESPONSE TEST ###########");
          }
        });
      }
    }
    return subId;
  }

  public static boolean isPresentUsersCognito(String valueFilter) {
    ListUsersResponse response = getUsersResponseWithFilter(valueFilter);

    if (Objects.isNull(response)) {
      logNoUsersFound(valueFilter);
      throwSkipException(valueFilter);
      return false;
    }

    if (response.users().isEmpty()) {
      return false;
    }

    logUsersInfo(valueFilter, response.users());
    return true;
  }

  private static void logNoUsersFound(String valueFilter) {
    log.info("No users exist in Cognito with emails: {}" + valueFilter);
  }


  private static void logUsersInfo(String valueFilter, List<UserType> users) {
    log.info(String.format("Users found with filter value %s:", valueFilter));
    users.forEach(user -> log.info(String.format("Username: %s, Status: %s, Created Date: %s",
      user.username(), user.userStatus(), user.userCreateDate())));
  }

  private static void throwSkipException(String valueFilter) {
    throw new SkipException(String.format("Not exist any user with emails %s into environments %s",
      valueFilter, getEnvironment()));
  }

}
