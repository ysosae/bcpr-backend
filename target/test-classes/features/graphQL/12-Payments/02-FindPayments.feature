@Graphql @Payments @secondChance @crt
Feature: GraphQL - Find Recent Payments

  Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-266-BCPRXRP-154-Find Recent Payments
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
    And I print size results with path data.findRecentPayments of the response graphql
    When The response is 200
    Then validate graphql record count data.findRecentPayments with DynamoDb records subId
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/findRecentPaymentsOutput.json
    And I compare response <Path> show the <Values>
      | LIST:data.findRecentPayments.lastFourDigits | NOT NULL                                                                              |
      | LIST:data.findRecentPayments.date           | NOT NULL                                                                              |
      | LIST:data.findRecentPayments.status         | OR:CONFIRMED/PENDING_CONFIRMATION/Authentication/INVALID_FIELD_RECEIVED_ROUTINGNUMBER |

  @IGNORE
  Scenario: BCPRXRP-1228-Get Find Recent Payments (Empty) - ES
    And delete item dynamoDb by table name Payment and item name Patria2023
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Patria2023       |
      | PASSWORD | Test**1234567890 |
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | ELemento no encontrado |

  @IGNORE
  Scenario: BCPRXRP-1227-Get Find Recent Payments (Empty) - EN
    And delete item dynamoDb by table name Payment and item name Patria2023
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Patria2023       |
      | PASSWORD | Test**1234567890 |
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | Not found |