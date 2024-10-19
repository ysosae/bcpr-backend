package model;

import static config.AbstractAPI.scenarioData;
import static storage.ScenarioContext.saveInScenarioContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class CardActivationValidator {
  private static final Logger log = Logger.getLogger(CardActivationValidator.class);

  public static void saveWrongLastEightDigits() {
   String value =  "12345647";
    if (scenarioData.has("wrongLastEightDigits")) {
      value = scenarioData.get("wrongLastEightDigits").getAsString();
    }

    if (StringUtils.isNotEmpty(value)) {
      if (!scenarioData.has("wrongLastEightDigits")) {
        saveInScenarioContext("wrongLastEightDigits", value);
      }
    } else {
      log.info("Cannot backup the user pass");
    }
  }

}
