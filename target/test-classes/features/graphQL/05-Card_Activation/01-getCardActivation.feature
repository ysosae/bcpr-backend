@Graphql  @secondChance @crt
Feature: Card Activation

  @ActivateCard
  Scenario: BCPRXRP-298 - Admin Card Deactivation
    Given Set AdminAndIsCreditCardExpiredFalse as main test user
#    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
#    Then I print out the results of the response
#    Then The response is 200
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    And Short wait between request
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    Then The response is 200

  @ActivateCard
  Scenario: BCPRXRP-297-BCPRXRP-50 - Get Card Activation
    Given Set AdminAndIsCreditCardExpiredFalse as main test user
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    And Short wait between request
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.cardActivation.status  | SUCCESS                                               |
      | data.cardActivation.message | OR:Successfully activated card ending/The card ending |

#  Scenario: BCPRXRP-298 - Admin Card Deactivation
#    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json
#    Then I print out the results of the Middleware response
#    Then The response code is 200
#    And I compare middleware response <Path> show the <Values>
#      | traceId      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
#      | data.status  | SUCCESS                                                                       |
#      | data.message | CONTAINS:Successfully deactivated card ending                                 |
#    And Short wait between request
#    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
#    Then I print out the results of the response
#    Then The response is 200
#
#  @ActivateCard
#  Scenario: BCPRXRP-297-BCPRXRP-50 - Get Card Activation
#    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json
#    Then I print out the results of the Middleware response
#    Then The response code is 200
#    And Short wait between request
#    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
#    Then I print out the results of the response
#    Then The response is 200
#    And I compare response <Path> show the <Values>
#      | data.cardActivation.status  | SUCCESS                                               |
#      | data.cardActivation.message | OR:Successfully activated card ending/The card ending |


  @IGNORE
  Scenario: BCPRXRP-1347 - Card Activation with expired card - EN
    Given Set isCreditCardExpiredTrue as main test user
    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    And Short wait between request
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    And delete item dynamoDb by table name LoginAttempts and item name username

  @IGNORE
  Scenario: BCPRXRP-1348 - Card Activation with expired card - ES
    Given Override language to es
    Given Set isCreditCardExpiredTrue as main test user
    Given post a middleware request using endpoint v1/AdminCardDeactivation and dynamic body middleware/middlewareGetCustomerInformationCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    And Short wait between request
    Given post a graphQL request using graphQL/getCardActivationMutation.graphql
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    And delete item dynamoDb by table name LoginAttempts and item name username