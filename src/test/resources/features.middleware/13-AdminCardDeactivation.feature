@Middleware
Feature: Middleware - AdminCardDeactivation

  Background:
    Given Set isCardActiveTrue as main test user

  Scenario: BCPRXRP-1044-Admin Card Deactivation (9958)
    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-validation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.status  | SUCCESS                                                                       |
      | data.message | CONTAINS:Successfully deactivated card ending                                 |
    Given post a middleware request using endpoint v1/CardActivation and body middleware/middlewareCardActivation.json

  Scenario: BCPRXRP-1045-Admin Card Deactivation (ERROR)
   Given post a middleware request using endpoint v1/AdminCardDeactivation with body middleware/middlewareGetCustomerInformationCommons.json override table values
      | institutionId  | B5642232            |
      | customerId     | 598226982           |
      | customerIdType | SSN                 |
      | accountId      | 0004199100010104009 |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-validation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | CONTAINS:INVALID INSTITUTION                                                  |