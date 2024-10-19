@Middleware
Feature: Middleware - CardValidation

  Background:
    Given Set yuliet as main test user

  Scenario: BCPRXRP-1042-Get Card Validation
    Given post a middleware request using endpoint v1/CardValidation and dynamic body middleware/middlewareCardValidation.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-validation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId             | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$  |
      | data.message        | The card has been validated correctly                                          |


  Scenario: BCPRXRP-1043-Get Card Validation (ERROR)
    Given post a middleware request using endpoint v1/CardValidation with body middleware/middlewareCardValidation.json override table values
      | institutionId               | BBBB4562                    |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-validation-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | CONTAINS:OR:422 Validation error/INVALID INSTITUTION                          |
