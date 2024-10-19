package storage;

import static config.AbstractAPI.scenarioData;

import org.apache.log4j.Logger;

public class ScenarioContext {
  private static final Logger log = Logger.getLogger(ScenarioContext.class);
  /**
   * Save in global variable to be used in tests along.
   *
   * @param key  name of the variable to be set.
   * @param value value of the variable to be set.
   */
  public static void saveInScenarioContext(String key, String value) {
    try {
      scenarioData.addProperty(key, value);
      log.info(String.format("Saved as Scenario Context key: %s with value: %s", key, value));
    } catch (Exception e) {
      log.error("Error saving to Scenario Context: " + e.getMessage());
    }
  }

  /**
   * get variable values saves on Scenario Data
   *
   * @param key name of variable to be set.
   */
  public static String getScenarioContextVariables(String key) {

    try {
      if (scenarioData != null && scenarioData.get(key) != null) {
        return scenarioData.get(key).toString();
      } else {
        log.info(String.format("The value stored in the key %s is %s", key, null));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return key;
  }
}
