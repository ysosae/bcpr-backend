@Graphql @Claims @secondChance @notification
Feature: Claims (1-5) - graphQL
  Description: For use Close claims is only for User with Role of Admin

  Background:
    Given Set Admin as main test user

  @Email
  Scenario: BCPRXRP-93-72-Submit Claim - Unauthorized charge - [EN]
    Given Override language to en
    Given Set facundo as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 1                                  |
      | id            | 1                                  |
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
    Given post a graphQL request using graphQL/closeClaim.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-1138-Submit Claim - Unauthorized charge - [ES]
    Given Override language to es
    Given Set facundo as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 1                                  |
      | id            | 1                                  |
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
    Given post a graphQL request using graphQL/closeClaim.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |


  @Email
  Scenario: BCPRXRP-94-Submit Claim - Duplicate transaction - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 2                           |
      | id            | 2                           |
      | value         | Using same payment method   |
      | transactionId | SCENARIO_DATA:transactionId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable
    And Short wait between request
    And add permission admins-group to username in cognito
#    Given post a graphQL request using graphQL/closeClaim.graphql
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  @notification
  Scenario: BCPRXRP-1300-Submit Claim - Duplicate transaction - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 2                                 |
      | id            | 2                                 |
      | value         | Using a different payment method  |
      | transactionId | SCENARIO_DATA:transactionId       |
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
  Scenario: BCPRXRP-103-71-Submit Claim - Wrong amount With attach Imagen - [EN]
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/getS3UploadUrl.graphql
    When The response is 200
    And I print out the results of the response
    Then save key publicUrl and path data.getS3UploadUrl.publicUrl as context variable
    Given Override language to en
    Given post graphQL request with body graphQL/listTransactionsWithSelectedDate.graphql override table values
      | fromDate | 2023-02-05 |
      | toDate   | 2023-04-05 |
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithImagen.graphql override table values
      | value         | SCENARIO_DATA:publicUrl     |
      | transactionId | SCENARIO_DATA:transactionId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.submitClaim.claimNumber | NOT NULL |
    Then save key claimId and path data.submitClaim.claimNumber as context variable

  @notification
  Scenario: BCPRXRP-95-Submit Claim - Wrong amount - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 3                           |
      | id            | 5                           |
      | value         | 2.00                        |
      | transactionId | SCENARIO_DATA:transactionId |
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
  Scenario: BCPRXRP-1301-Submit Claim - Wrong amount - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 3                           |
      | id            | 5                           |
      | value         | 2.00                        |
      | transactionId | SCENARIO_DATA:transactionId |
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
  Scenario: BCPRXRP-1302-Submit Claim - ATM did not issue the money - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 4                                  |
      | id            | 9                                  |
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
  Scenario: BCPRXRP-96-Submit Claim - ATM did not issue the money - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 4                                  |
      | id            | 9                                  |
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
  Scenario: BCPRXRP-97-Submit Claim - ATM did not issue the total money - [EN]
    Given Override language to en
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to en
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 5                           |
      | id            | 10                          |
      | value         | 2.00                        |
      | transactionId | SCENARIO_DATA:transactionId |
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
  Scenario: BCPRXRP-96-Submit Claim - ATM did not issue the total money - [ES]
    Given Override language to es
    Given Set hasTrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    Then from data.listTransactions.transactions search for claimNumber that contain null and save id value as transactionId
    Given Override language to es
    Given post graphQL request with body graphQL/submitClaimWithoutImagen.graphql override table values
      | claimTypeId   | 5                           |
      | id            | 10                          |
      | value         | 2.00                        |
      | transactionId | SCENARIO_DATA:transactionId |
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


