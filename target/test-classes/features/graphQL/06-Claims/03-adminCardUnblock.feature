@Graphql @Claims @secondChance @notification
Feature: Admin Card unblock - graphQL
  Description: For use Admin Card unblock is only for User with Role of Admin and for claim Unauthorized charge type

  @Email
  Scenario: BCPRXRP-1523 - Admin Card unblock - Success Graphql [EN]
    Given Override language to en
    Given Set Admin as main test user
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
    And add permission admins-group to username in cognito
    Given post a graphQL request using graphQL/adminCardFraudUnblockMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    Then I compare response <Path> show the <Values>
      | data.adminCardFraudUnblock.status  | SUCCESSFUL                                  |
      | data.adminCardFraudUnblock.message | CONTAINS:Successfully unblocked card ending |
    And Short wait between request
    Given post graphQL request with body graphQL/closeClaim.graphql override table values
      | claimId   | SCENARIO_DATA:claimId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.closeClaim.claimId | NOT NULL |
      | data.closeClaim.status  | CLOSE    |

  Scenario: BCPRXRP-1524 - Admin Card unblock with exist block
    Given Set isNotBlockedFalse as main test user
    Given post a graphQL request using graphQL/adminCardFraudUnblockMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |

  Scenario: BCPRXRP-1525 - Admin Card unblock using token Not Admin
    Given Set NoAdmin as main test user
    Given post a graphQL request using graphQL/adminCardFraudUnblockMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |






