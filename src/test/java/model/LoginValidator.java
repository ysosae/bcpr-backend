package model;

import static config.AbstractAPI.scenarioData;
import static storage.ScenarioContext.saveInScenarioContext;
import static utils.DataGenerator.randomSetOldPolicyValuePassword;
import static utils.DataGenerator.randomUsername;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class LoginValidator {
  private static final Logger log = Logger.getLogger(LoginValidator.class);

  public static void saveWrongPassword() {
   String value = randomSetOldPolicyValuePassword("Test");
    if (scenarioData.has("wrongPassword")) {
      value = scenarioData.get("wrongPassword").getAsString();
    }

    if (StringUtils.isNotEmpty(value)) {
      if (!scenarioData.has("wrongPassword")) {
        saveInScenarioContext("wrongPassword", value);
      }
    } else {
      log.info("Cannot backup the user pass");
    }
  }

  public static void saveWrongUsername() {
    String value = randomUsername(8);
    if (scenarioData.has("wrongUsername")) {
      value = scenarioData.get("wrongUsername").getAsString();
    }

    if (StringUtils.isNotEmpty(value)) {
      if (!scenarioData.has("wrongUsername")) {
        saveInScenarioContext("wrongUsername", value);
      }
    } else {
      log.info("Cannot backup the user pass");
    }
  }

  public static void saveWithEmailAndPassword() {
    String value = "perficientclienttest@gmail.com";
    if (scenarioData.has("validEmail")) {
      value = scenarioData.get("validEmail").getAsString();
    }

    if (StringUtils.isNotEmpty(value)) {
      if (!scenarioData.has("validEmail")) {
        saveInScenarioContext("validEmail", value);
      }
    } else {
      log.info("Cannot backup the user pass");
    }
  }

}
