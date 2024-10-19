package model;

import static config.RestAssuredExtension.removeCharacter;

import org.apache.log4j.Logger;
import storage.RewardsStorage;

public class RewardsValidator {
  private static final Logger log = Logger.getLogger(RewardsValidator.class);
  private final RewardsStorage rewardsStorage;
  private final int threshold;

  public RewardsValidator(RewardsStorage rewardsStorage, int threshold) {
    this.rewardsStorage = rewardsStorage;
    this.threshold = threshold;
  }

  public void validateRewards() {
    try {
      int rewardsPoints = Integer.parseInt(removeCharacter(rewardsStorage.getScenarioVariable("rewardsPoints")));
      boolean canRedeemPoints = Boolean.parseBoolean(removeCharacter(rewardsStorage.getScenarioVariable("canRedeemPoints")));

      rewardsStorage.storeRewardsValue("rewardsPoints", rewardsPoints);
      rewardsStorage.storeRewardsValue("canRedeemPoints", canRedeemPoints);

      boolean isValid = validateCanRedeemPoints(rewardsPoints, canRedeemPoints);
      log.info("Validation Result for Can Redeem Points: " + isValid);

    } catch (NumberFormatException e) {
      log.error("The rewardsPoints or canRedeemPoints is not valid: " + e.getMessage());
      throw new IllegalArgumentException("Invalid rewardsPoints or canRedeemPoints: " + e.getMessage());
    }
  }

  public boolean validateCanRedeemPoints(int rewardsPoints, boolean canRedeemPoints) {
    return rewardsPoints >= threshold && canRedeemPoints;
  }
}
