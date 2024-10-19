package storage;

import static storage.ScenarioContext.getScenarioContextVariables;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class RewardsStorage {
  private static final Logger log = Logger.getLogger(RewardsStorage.class);
  private final Map<String, Object> rewards = new HashMap<>();

  public void storeRewardsValue(String key, Object value) {
    if (StringUtils.equalsIgnoreCase(key, "rewardsPoints") ||
      StringUtils.equalsIgnoreCase(key, "canRedeemPoints")) {

      rewards.put(key, value);
      log.info(String.format("Key '%s' was stored with value '%s'%n", key, value));
    }
  }

  public String getScenarioVariable(String key) {
    return getScenarioContextVariables(key);
  }

  public Object getRewardsValue(String key) {
    return rewards.get(key);
  }
}
