@Graphql @Claims @secondChance @notification
Feature: Claims (5-10) - graphQL
  Description: For use Close claims is only for User with Role of Admin

  Background:
    Given Set Admin as main test user

  @Email
  Scenario: BCPRXRP-98-Submit Claim - Credit not processed - [EN]
    Given Override language to en
    Given Set facundo as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 6                                  |
      | id            | 14                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    Given post a middleware request using endpoint v1/AdminCardUnlock and dynamic body middleware/middlewareAdminCardUnlock.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-unlock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.status  | SUCCESSFUL                                                                    |
      | data.message | CONTAINS:Successfully unblocked card ending                                   |
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-1304-Submit Claim - Credit not processed - [ES]
    Given Override language to es
    Given Set facundo as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 6                                  |
      | id            | 14                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    Given post a middleware request using endpoint v1/AdminCardUnlock and dynamic body middleware/middlewareAdminCardUnlock.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/card-unlock-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.status  | SUCCESSFUL                                                                    |
      | data.message | CONTAINS:Successfully unblocked card ending                                   |
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |


  @Email
  Scenario: BCPRXRP-1305-Submit Claim -Assets or services not provided. - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 7                                  |
      | id            | 16                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-99-Submit Claim -Assets or services not provided. - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 7                                  |
      | id            | 16                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-1302-Submit Claim - Transaction canceled - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 8                                  |
      | id            | 18                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-102-Submit Claim - Transaction canceled - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 8                                  |
      | id            | 18                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-100-Submit Claim - Defective assets or services - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 9                                  |
      | id            | 20                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-xxx-Submit Claim - Defective assets or services - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 9                                  |
      | id            | 20                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |


  @notification @crt
  Scenario: BCPRXRP-101-Submit Claim - Other - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 10                                 |
      | id            | 21                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @Email
  Scenario: BCPRXRP-1306-Submit Claim - Other - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 10                                 |
      | id            | 21                                 |
      | value         | Comentarios adicionales (opcional) |
      | transactionId | SCENARIO_DATA:transactionId        |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |


  @IGNORE
  Scenario: BCPRXRP-74-Close Claim
    Given post a graphQL request using graphQL/closeClaim.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |


