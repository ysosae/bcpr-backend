@Graphql @secondChance @Payments @crt
Feature: GraphQL - List Wallet


  Scenario: BCPRXRP-264-150-323-Get List of Wallet Accounts By Mutation
    Given Set facundo as main test user
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    And I print size results with path data.listWalletAccounts of the response graphql
    When The response is 200
    Then validate graphql record count data.listWalletAccounts with DynamoDb records subId
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId  | NOT NULL |
      | LIST:data.listWalletAccounts.routing    | NOT NULL |
     # | LIST:data.listWalletAccounts.lastFourDigitsAccountNumber  | NOT NULL                                |
      | LIST:data.listWalletAccounts.accType    | NOT NULL |
      | LIST:data.listWalletAccounts.customName | NOT NULL |
      | LIST:data.listWalletAccounts.status     | NOT NULL |

  @IGNORE
  Scenario: BCPRXRP-1217-Get List of Wallet Accounts (Empty) - EN
    And delete item dynamoDb by table name Wallet and item name Patria2023
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Patria2023       |
      | PASSWORD | Test**1234567890 |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | Not found |

  @IGNORE
  Scenario: BCPRXRP-1218-Get List of Wallet Accounts (Empty) - ES
    And delete item dynamoDb by table name Wallet and item name Patria2023
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Patria2023       |
      | PASSWORD | Test**1234567890 |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | ELemento no encontrado |
