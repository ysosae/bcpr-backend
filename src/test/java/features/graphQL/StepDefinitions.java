package features.graphQL;

import com.google.gson.Gson;
import common.CommonLambdaFrontendAPIKeyValueConstant;
import config.AbstractAPI;
import config.RestAssuredExtension;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.LinkedHashMap;
import model.RewardsValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;

import java.util.List;
import storage.RewardsStorage;

import static config.CognitoAWS.isPresentUsersCognito;
import static config.RestAssuredExtension.*;

public class StepDefinitions extends AbstractAPI {
    private static final Logger log = Logger.getLogger(StepDefinitions.class);
    private final RewardsStorage rewardsStorage = new RewardsStorage();
    private final RewardsValidator rewardsValidator = new RewardsValidator(rewardsStorage,
   Integer.parseInt(CommonLambdaFrontendAPIKeyValueConstant.THRESHOLD_REWARDS_LIMIT) );

    @Given("^post recursive a graphQL request using (.*)$")
    public void graphQLRequestRecursive(String body) {
        for (int i = 0; i < 10; i++) {
            response = postMethodGraphQL(body);
            response = postMethodGraphQL("graphQL/getEnrollmentValidationYohara.graphql");
            Assert.assertTrue(
                    StringUtils.containsIgnoreCase(String.valueOf(response.statusCode()), "200"),
                    String.format(
                            "The Status Code response is different to expected, obtained: %s, expected: %s",
                            response.statusCode(), "200"));
        }

    }

    @Then("^I validate API response with Schema statement referenced at (.*)$")
    public void iValidateAPIResponseWithSchemaStatement(String schemaPath) {
        rest.matchesJsonSchemaValidator(response, schemaPath);
    }

    @Given("^post a graphQL request with body (.*) override table values$")
    public void postAGraphQLRequestUsingOverrideTableValues(String body, List<List<String>> t_table) {
        overrideData = null;
        response = null;
        overrideData = setOverrideData(t_table);
        response = postMethodGraphQL(body);
    }

    @Given("^perform login using data in (.*)$")
    public void performLoginUsingDataInGraphQLLoginGraphql(String body) {
        response = null;
        RestAssuredExtension.setSessionUser("");
        response = authenticationGraphQL(body);
    }

    @Given("^Override language to (.*)$")
    public void overrideLanguageToEs(String language) {
        overrideLanguage = language;
    }

    @Then("^save response as context variable$")
    public void saveResponseAsContextVariable() {
      Object value = response.getBody().jsonPath().get(); // Obtén el valor de la respuesta

      if (value instanceof LinkedHashMap) {
        String jsonString = new Gson().toJson(value);
        scenarioResponse.addProperty("response", jsonString);
      } else if (value instanceof String) {
        scenarioResponse.addProperty("response", (String) value);
      } else {
        log.error("Unexpected response type: " + value.getClass().getName());
      }
    }

    @Then("^save key (.*) and path (.*) as context variable$")
    @Then("^save key (.*) and value (.*) as context variable$")
    public void savePathDataAsContextVariable(String key, String path) {
      String value = response.getBody().jsonPath().getString(path);
      scenarioData.addProperty(key, value);
        log.info(String.format("The key %s was stored with value %s%n",
            key, response.getBody().jsonPath().getString(path)));
    }

    @Then(
            "^from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED$")
    public void searchKeyFromResponseThatContains(List<List<String>> t_table) {
        compareResponseThatContains(t_table);
    }

    @Then("^from (.*) search for (.*) that contain (.*) and save (.*) value as (.*)$")
    public void searchKeyFromResponseThatContainsAndSave(
            String path, String filterKey, String thatContainsKey, String saveKey, String saveAs) {
        saveKeyFromResponseThatContains(path, filterKey, thatContainsKey, saveKey, saveAs);
    }

    @Then("^path (.*) and save value as (.*)$")
    public void searchKeyFromResponseThatContainNumbersAndSaveNew(String path, String saveAs) {
        saveKeyOffsetFromResponse(path, saveAs);
    }

    @Then("^in path (.*) search for (.*) that contain (.*) and validate following$")
    public void inPathDataListSearchForContainAndValidateFollowing(
            String path, String key, String thatContainsKey, List<List<String>> t_table) {
        searchPathContain(path, key, thatContainsKey, t_table);
    }

    @Then("^I wait during (.*) seconds before send the code$")
    public void iWaitDuringSecondsBeforeSendTheCode(int timeInSec) {
        shortWait(timeInSec);
    }

    @Given("^Searching with the email (.*) filter is a user exists in cognito$")
    public void searchingWithFilterIsAUserExistsInCognito(String valueFilter) {
        Assert.assertTrue(isPresentUsersCognito(valueFilter));
    }

    @And("^clean token of (.*) user$")
    public void cleanTokenOfAutomationUser(String user) {
        if(StringUtils.equalsIgnoreCase(user,"username")){
            user= getSessionUser();
        }
        cleanUserTokens(user);
    }

    @Given("^filter attribute (.*) with (.*) for validate test user$")
    public void filterAttributeEmailForValidateTestUser(String attributeName, String attributeValue) {
        selectCognitoUser(attributeName, attributeValue);
    }

    @And("^save cognito attribute (.*) by username as context variable$")
    public void saveCognitoAttributeSubByUsernameAsContextVariable(String attributeName) {
        if (scenarioData.has(attributeName)) {
      scenarioData.addProperty(attributeName,getSubId(attributeName));
        } else {
          saveValueAttributeCognito(attributeName);
        }
        log.info(
                String.format(
                        "The attribute %s was stored with username %s",
                        attributeName, username));
    }


    @Given("^save user with Role (.*) as main test user$")
    public void saveUserRoleOfTokenJWTFacundoAsMainTestUser(String role) {
        getUserRoleTokenJWT(role);
    }

  @And("validate multiple IVR call center")
  public void theInformationIsValidated() {
    validateIVRCallCenter();
   }

  @And("validate can redeem poinst into Rewards")
  public void validateCanRedeemPoinstIntoRewards() {
    rewardsValidator.validateRewards();
  }
}
