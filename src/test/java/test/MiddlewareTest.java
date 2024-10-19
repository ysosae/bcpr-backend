package test;

import static config.RestAssuredExtension.getUserData;
import static enums.FilesPath.MIDDLEWARE_USERS_FILE_LOCATION;
import static utils.CognitoUserHandler.saveInJsonFile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import config.AbstractAPI;
import config.RestAssuredExtension;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.util.Map;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class MiddlewareTest extends AbstractAPI {
  private static final Logger log = Logger.getLogger(MiddlewareTest.class);
  public static ResponseOptions<Response> response = null;
  public static RestAssuredExtension rest = new RestAssuredExtension();


  @Test
  public void validateShortTimeCondition() {
    response = postMethodMiddleware("v1/ListInstitutionStatus", "middleware/middlewareListInstitutionStatus.json");
    boolean isDataResponse= response.getBody().jsonPath().get("data.message").equals("Token Success..");
    shortWait(5, !isDataResponse);
  }

    @Test
    public void retrieveDataMiddleware() {
        JsonObject usersBundle = getUserData();
        JsonObject newUsersBundle = new JsonObject();
        Gson gson = new Gson();

        if (!usersBundle.entrySet().isEmpty()) {
            for (Map.Entry<String, JsonElement> entry : usersBundle.entrySet()) {
                String key = entry.getKey();
                RestAssuredExtension.setSessionUser(key);
                JsonObject keyValue = entry.getValue().getAsJsonObject();
                JsonObject combinedData = new JsonObject();
                combinedData.add("userData", keyValue);

                try {
                    ResponseOptions<Response> responseOptions = postMethodMiddleware("v1/GetAccountDetails", "middleware/middlewareGetCustomerInformationCommons.json");

                   if (responseOptions.getBody() != null && responseOptions.getBody().jsonPath().get("data") != null) {
                        JsonObject details = gson.toJsonTree(responseOptions.getBody().jsonPath().getJsonObject("data")).getAsJsonObject();
                        combinedData.add("apiDetails", details);
                    } else {
                        combinedData.addProperty("apiDetails", "Error: No valid data in response");
                    }
                } catch (Exception e) {
                    log.error("Error processing entry for key " + key + ": " + e.getMessage());
                    combinedData.addProperty("apiDetails", "Error: " + e.getMessage());
                }

                newUsersBundle.add(key, combinedData);
            }
        }
        saveInJsonFile(MIDDLEWARE_USERS_FILE_LOCATION.getText(), newUsersBundle);
    }
}

