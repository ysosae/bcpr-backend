package enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum defining file paths for various resources.
 */
@Getter
@RequiredArgsConstructor
public enum FilesPath {
  GLOBAL_DATA_FILE_LOCATION("test.properties"),
  COGNITO_USERS_FILE_LOCATION ("src/test/resources/data/cognitoUsers.json"),
  MIDDLEWARE_USERS_FILE_LOCATION ("src/test/resources/data/middlewareUsers.json"),
  USERS_FILE_LOCATION ("src/test/resources/data/users.json"),
  TRANSLATIONS_FILE_EN_PATH("src/test/resources/data/data-test/en.json"),
  TRANSLATIONS_FILE_ES_PATH("src/test/resources/data/data-test/es.json"),
  ALLURE_REPORT_FILE_LOCATION ("target/allure-results/environment.properties");

  private final String text;


  @Override
  public String toString() {
    return text;
  }
}
