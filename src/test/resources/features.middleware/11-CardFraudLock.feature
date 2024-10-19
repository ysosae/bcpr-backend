@Middleware
Feature: Middleware - CardFraudLock

  Background:
    Given Set Facundo2023 as main test user

  Scenario: BCPRXRP-1032-Card Fraud Lock
    Given post a middleware request using endpoint v1/CardFraudLock with body middleware/middlewareCardLockCommons.json override table values
      | action | LOCK |
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-fraud-lock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                        |
      | data.status  | OR:SUCCESS/PARTIALBLK                                                                                |
      | data.message | OR:Successfully turned card off/do not complete the block/unblock process/ERROR ALREADY BLOCK....... |
    Given post a middleware request using endpoint v1/AdminCardUnlock and dynamic body middleware/middlewareAdminCardUnlock.json

  Scenario: BCPRXRP-1034-Card Fraud Lock (ERROR)
    Given post a middleware request using endpoint v1/CardTemporaryLock with body middleware/middlewareCardLockCommons.json override table values
      | action    | LOCK                |
      | accountId | 0000000000000000000 |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-fraud-lock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | ORG-ACCT NOT FOUND                                                            |