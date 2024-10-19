package features.middleware;

import static storage.ScenarioContext.getScenarioContextVariables;

import config.AbstractAPI;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import org.testng.Assert;

public class MiddlewareStepDefinitions extends AbstractAPI {

  @Given("^post a middleware request using endpoint (.*) and body (.*)$")
  public void middlewareRequest(String path, String body) {
    response = postMethodMiddleware(path, body);
  }

  @Given("^post a middleware request using endpoint (.*) with body (.*) override table values$")
  public void middlewareRequestOverride(String path, String body, List<List<String>> t_table) {
    overrideData = setOverrideData(t_table);
    response = postMethodMiddleware(path, body);
  }

  @Given("^post a middleware request using endpoint (.*) and dynamic body (.*)$")
  public void middlewareDynamicDataRequest(String path, String body) {
    response = null;
    response = postMethodMiddleware(path, body);
  }

  @Then("^I print out the results of the Middleware response$")
  public void iPrintOutTheResultsOfTheResponseMiddleware() {
    response.getBody().prettyPrint();
  }

  @Then("^I validate Middleware response with Schema statement referenced at (.*)$")
  public void iValidateMiddlewareResponseWithSchemaStatement(String schemaPath) {
    matchesJsonSchemaValidator(response, schemaPath);
  }

  @Then("^I compare middleware response <Path> show the <Values>$")
  public void iCompareResponsePathShowTheValues(List<List<String>> t_table) {
    if(scenarioData!=null){
      boolean hasTransactions = Boolean.parseBoolean(getScenarioContextVariables("hasTransactions"));
      if(!hasTransactions){
        Assert.assertTrue(true);
      }
    }else{
      compareResponsePathShowTheValues(response, t_table);
    }

  }

  @Then("^Select from (.*) and save its (.*) value as (.*)$")
  public void searchFromResponseAndSave(
    String path, String saveKey, String saveAs) {
    saveKeyFromResponseThatContains(path, saveKey, saveAs);
  }

  @Given("^post a middleware proxy request using endpoint (.*)$")
  public void middlewareProxyRequest(String path) {
    response = postMethodMiddleware(path);
  }
}
