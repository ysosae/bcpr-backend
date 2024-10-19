@Middleware
Feature: Middleware - AdminCardUnblock

  Background:
    Given Set Facundo2023 as main test user

  Scenario: BCPRXRP-1033-Admin Card Unblock
    Given post a middleware request using endpoint v1/CardFraudLock with body middleware/middlewareCardLockCommons.json override table values
      | action | LOCK |
    Given post a middleware request using endpoint v1/AdminCardUnlock and dynamic body middleware/middlewareAdminCardUnlock.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-unlock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId             | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.status         | SUCCESSFUL                                                                    |
      | data.message        | CONTAINS:Successfully unblocked card ending                                   |

  Scenario: BCPRXRP-1035-Admin Card Unblock (ERROR)
    Given post a middleware request using endpoint v1/AdminCardUnlock with body middleware/middlewareAdminCardUnlock.json override table values
      | institutionId               | B5642232                     |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-unlock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                       | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$  |
      | errors[0].message             | CONTAINS:INVALID INSTITUTION                                                   |