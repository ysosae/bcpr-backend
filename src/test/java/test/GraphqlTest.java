package test;

import static config.RestAssuredExtension.*;

import config.AbstractAPI;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.*;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class GraphqlTest extends AbstractAPI {
  private static final Logger log = Logger.getLogger(GraphqlTest.class);
  public static ResponseOptions<Response> response = null;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Test
  public void executeLoginPRD() {

    try {
      RestAssured.given()
          .config(
              RestAssured.config()
                  .httpClient(
                      io.restassured.config.HttpClientConfig.httpClientConfig()
                          .setParam(
                              "http.connection.timeout",
                              80000) // Connection timeout in milliseconds
                          .setParam(
                              "http.socket.timeout",
                              80000))); // Socket read timeout in milliseconds
      setDefaultHeaders();
      setBodyGraphql("graphQL/loginPRD.graphql");
      try {
        builder.setBaseUri(
            "https://bcpr-api.evertecinc.com/ah1hz2h2fi.execute-api.us-east-1.amazonaws.com");

        request = RestAssured.given().spec(builder.build());
        response = request.post(new URI("/prd"));

      } catch (URISyntaxException e) {
        log.error(e.getMessage());
      }
      log.info("Response Success:" + response.statusCode());
    } catch (NullPointerException e) {
      log.error("Path is invalid: " + e.getMessage());
    }
  }
}
