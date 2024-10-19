@Middleware
Feature: Middleware - CardTemporaryLock

  Background:
    Given Set Facundo2023 as main test user

  Scenario: BCPRXRP-629-Card Temporary Lock (9958)
    Given post a middleware request using endpoint v1/CardTemporaryLock with body middleware/middlewareCardLockCommons.json override table values
      | action | LOCK |
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-temporary-lock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                                 |
      | data.status  | OR:SUCCESS/PARTIALBLK                                                                                         |
      | data.message | CONTAINS:OR:do not complete the block/unblock process/Successfully turned card off/ERROR ALREADY BLOCK....... |

  Scenario: BCPRXRP-630-Card Temporary Unlock (9958)
    Given post a middleware request using endpoint v1/CardTemporaryLock with body middleware/middlewareCardLockCommons.json override table values
      | action | UNLOCK |
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-temporary-lock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                       |
      | data.status  | OR:SUCCESS/PARTIALULK                                                                               |
      | data.message | CONTAINS:OR:do not complete the block/unblock process/Successfully turned card on/ INV ULK BCPRBFRD |


  Scenario: BCPRXRP-631-Card Temporary Lock (ERROR)
    Given post a middleware request using endpoint v1/CardTemporaryLock with body middleware/middlewareCardLockCommons.json override table values
      | action    | UNLOCK              |
      | accountId | 0000000000000000000 |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-temporary-lock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | ORG-ACCT NOT FOUND                                                            |