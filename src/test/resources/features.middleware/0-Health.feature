@Middleware
Feature: Middleware - Proxy - Microservices

  Background:
    Given Set facundo as main test user
  @IGNORE
  @MiddlewareProxy
  Scenario: BCPRXRP-1050-Launch health check status and version
    Given post a middleware proxy request using endpoint /health
    Then I print out the results of the Middleware response
    Then The response code is 200
    And I compare middleware response <Path> show the <Values>
      | status                 | UP                            |
      | checks[0].name         | Middleware-proxy microservice |
      | checks[0].status       | UP                            |
      | checks[0].data.version | 1.1.3                         |





