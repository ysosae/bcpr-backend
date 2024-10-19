@Graphql  @secondChance @crt
Feature: Card Activation - Admin deactivation

  @ActivateCard
  Scenario: BCPRXRP-1496 - Admin Card Deactivation - Success Graphql
    Given Set Admin as main test user
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    Then The response is 200
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    And I compare middleware response <Path> show the <Values>
      | data.adminCardDeactivation.status  | SUCCESS                                                                       |
      | data.adminCardDeactivation.message | CONTAINS:Successfully deactivated card ending                                 |
    And Short wait between request
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    Then The response is 200

  @ActivateCard
  Scenario: BCPRXRP-1497 - Admin Card Deactivation with expired card
    Given Set isCreditCardExpiredTrue as main test user
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    And delete item dynamoDb by table name LoginAttempts and item name username

  @ActivateCard
  Scenario: BCPRXRP-1498 - Admin Card Deactivation using token Not Admin
    Given Set NoAdmin as main test user
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    And delete item dynamoDb by table name LoginAttempts and item name username