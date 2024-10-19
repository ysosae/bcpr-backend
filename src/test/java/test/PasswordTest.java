package test;

import static config.RestAssuredExtension.writtenBodyFromResource;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static utils.DataGenerator.randomPassword;
import static utils.DataGenerator.randomUsername;
import static utils.ValidRegex.isValidRegex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import config.AbstractAPI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.annotations.Test;
import utils.ValidRegex;

public class PasswordTest extends AbstractAPI {
  private static final Logger log = Logger.getLogger(PasswordTest.class);

  @Test
  public void getPassword() {
    String password = randomPassword(12);
    log.info(password);
  }

  @Test
  public void getUsername() {
    String username = randomUsername();
    log.info(username);
  }

  @Test
  public void writtenNewPassword() {
    String password = randomPassword(8);
    String path = "src/test/resources/data/users.json";

    String usersPath;
    try {
      usersPath = new String(Files.readAllBytes(Paths.get(path)));
    } catch (IOException e) {
      log.error("Failed to read file at path: " + path, e);
      return;
    }

    JsonObject json;
    try {
      json = JsonParser.parseString(usersPath).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      log.error("Failed to parse JSON from file at path: " + path, e);
      return;
    }

    JsonObject dataUser = json.getAsJsonObject("automation");
    JsonArray loginData = dataUser.getAsJsonArray("login");
    loginData.set(1, new JsonPrimitive(password));
    dataUser.add("login", loginData);
    json.add("automation", dataUser);

    writtenBodyFromResource(path, json.toString());
  }


  @ParameterizedTest(name = "#{index} - Run test with password = {0}")
  @MethodSource("validPasswordProvider")
  void test_password_regex_valid(String password) {
    if (isValidPassword(password)) {
      assertTrue(isValidPassword(password));
      log.info(password + " VALID");
    } else {
      assertFalse(isValidPassword(password));
      log.info(password + " INVALID");
    }
  }

  static Stream<String> validPasswordProvider() {
    return Stream.of(
      "Test123456**",
      "Marisol2025,123",
      "JAVAregex",
      "java.regex",
      "java-regex",
      "Marisol2025-.123",
      "java.regex.123",
      "java-regex-123",
      "Java_regex_123",
      "Yuliet123432--**",
      "123456",
      "java123",
      "Autom123456**",
      "Autom123456**",
      "Zx2,TpI.}KU",
      "Wx6*04CnMhk5",
      "Java123456789101112234565832521321352325",
      "01234567890123456789");
  }

  @ParameterizedTest(name = "#{index} - Run test with username = {0}")
  @MethodSource("validUsernameProvider")
  void test_username_regex_valid(String username) {
    if (isValid(username)) {
      assertTrue(isValid(username));
      log.info(username + " VALID");
    } else {
      assertFalse(isValid(username));
      log.info(username + " INVALID");
    }
  }

  static Stream<String> validUsernameProvider() {
    return Stream.of(
      "Yuliet9103",
      "Marisol2025,123",
      "Marisol2025-.123",
      "java.regex",
      "java-regex",
      "java_regex",
      "java.regex.123",
      "java-regex-123",
      "java_regex_123",
      "Java123432",
      "Marisol202123",
      "java123 ",
      "Ld4vSYqv",
      "Autom123456**",
      "Java123456789101112234565832521321352325",
      "1235321532152");
  }

  public static boolean isValidPassword(String password) {
    boolean result = false;

    if (doesNotContainUsername(password, "Yuliet") && isValidRegex(password, ValidRegex.newPolicyPasswordRegex)) {
      result = true;
      log.info("VALID");
    } else {
      log.info("INVALID");
    }
    return result;
  }

  public static boolean doesNotContainUsername(String password, String username) {
    return !StringUtils.contains(password, username);
  }

  public static boolean isValid(String label) {
    boolean result = false;

    if (StringUtils.containsNone(label, ValidRegex.characterSpecialRegex) &&  isValidRegex(label,ValidRegex.newPolicyPasswordRegex)) {
      result = true;
      log.info(" VALID " + label);
    } else {
      log.info(" INVALID " + label);
    }
    return result;
  }
}
