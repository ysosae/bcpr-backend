@Middleware
Feature: Middleware - CardActivation

  Background:
    Given Set yuliet as main test user

  Scenario: BCPRXRP-637-Card Activation (9958)
    Given post a middleware request using endpoint v1/CardActivation and body middleware/middlewareCardActivation.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-activation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$         |
      | data.status  | OR:SUCCESS/PARTIALACT                                                                 |
      | data.message | CONTAINS:OR:The card ending in/do not complete activation process/Successfully activated card ending in |
    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json

  Scenario: BCPRXRP-638-Card Activation (Error)
#    Given post a middleware request using endpoint v1/CardActivation and body middleware/middlewareGetCustomerInformationCommons.json
    Given post a middleware request using endpoint v1/CardActivation with body middleware/middlewareCardActivation.json override table values
      | customerId    | 582910408      |
      | institutionId | cc012265567424 |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-activation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | OR:422 Validation error/Server 1 not accesible/ORG-ACCT NOT FOUND             |
