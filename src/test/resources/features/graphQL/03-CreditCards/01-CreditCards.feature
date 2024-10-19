@Graphql @secondChance @crt
Feature: Credit Cards

  Scenario: BCPRXRP-291-Get credit cards list from logged user
    Given Set isUserCreation as false
    Given Set yuliet as main test user
   #Given filter attribute email with perficientclienttest@gmail.com for validate test user
   # Given filter attribute phone_number with +17862587565 for validate test user
    Given post a graphQL request using graphQL/listCreditsCards.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listCreditsCards.id             | NOT NULL                                                    |
      | LIST:data.listCreditsCards.network        | OR:VISA/MASTERCARD                                          |
#     | LIST:data.listCreditsCards.currentBalance  | AMOUNT             |
#      | LIST:data.listCreditsCards.availableCredit | AMOUNT             |
      | LIST:data.listCreditsCards.last4Digits    | NUMBER                                                      |
      | LIST:data.listCreditsCards.last4Digits    | REGEX:^[0-9]{4}$                                            |
      | LIST:data.listCreditsCards.blockingStatus | OR:NoBlocked/TemporaryBlocked/OtherTypeOfBlock/FraudBlocked |
    Then save response as context variable
    Then save key last4Digits and path data.listCreditsCards[0].last4Digits as context variable
    Then save key rewardsPoints and path data.listCreditsCards[0].rewards.rewardsPoints as context variable
    Then save key canRedeemPoints and path data.listCreditsCards[0].rewards.canRedeemPoints as context variable
    And validate can redeem poinst into Rewards

  @IGNORE
  Scenario: BCPRXRP-503-Recursive LisCreditCards
    Given post recursive a graphQL request using graphQL/listCreditsCards.graphql
