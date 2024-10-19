@Graphql  @secondChance @crt
Feature: Card Data Status By Mutation

  Scenario: BCPRXRP-300-Card Data Status By Mutation ON
     Given Set Facundo2023 as main test user
    Given post graphQL request with body graphQL/getCardDataStatusByMutation.graphql override table values
      | switchTo | ON|
    Then I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.switchCardTemporaryBlockStatus.currentStatus | ON |


  Scenario: BCPRXRP-299-Card Data Status By Mutation OFF
    Given Set Facundo2023 as main test user
    Given post graphQL request with body graphQL/getCardDataStatusByMutation.graphql override table values
      | switchTo | OFF |
    Then I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.switchCardTemporaryBlockStatus.currentStatus | OFF |