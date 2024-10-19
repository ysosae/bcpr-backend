@Graphql @secondChance @Payments @notification @crt
Feature: GraphQL-Add Wallets

  Description: those test must be executed together

  Background:
    Given Set Yuliet as main test user

  @Email
  Scenario: BCPRXRP-253-151-Add Wallet Account By Mutation
    Given Override language to en
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | accType    | s                  |
      | customName | QA-Wallet-Checking |
    And I print out the results of the response
    When The response is 200
   Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId   | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable

  Scenario: BCPRXRP-261-152-Revalidate WalletAccount By Mutation
    #Given post a graphQL request using graphQL/revalidateWalletAccount.graphql
    Given post graphQL request with body graphQL/revalidateWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
   Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/revalidateWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.revalidateWalletAccount.accountId   | NOT NULL |
      | data.revalidateWalletAccount.expiration  | NOT NULL |
      | data.revalidateWalletAccount.linkToken   | NOT NULL |
      | data.revalidateWalletAccount.requestId   | NOT NULL |

  Scenario: BCPRXRP-263-155-Confirm Wallet Account By Mutation
    #Given post a graphQL request using graphQL/confirmWalletAccount.graphql
    Given post graphQL request with body graphQL/confirmWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/confirmWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.confirmWalletAccount.accountId      | NOT NULL |
      | data.confirmWalletAccount.status         | NOT NULL |

  Scenario: BCPRXRP-264-Get List of Wallet Accounts By Mutation
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    And I print size results with path data.listWalletAccounts of the response graphql
    When The response is 200
#   Then validate graphql record count data.listWalletAccounts with DynamoDb records subId
#   Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId  | NOT NULL |
      | LIST:data.listWalletAccounts.routing    | NOT NULL |
     # | LIST:data.listWalletAccounts.lastFourDigitsAccountNumber  | NOT NULL                                |
      | LIST:data.listWalletAccounts.accType    | NOT NULL |
      | LIST:data.listWalletAccounts.customName | NOT NULL |
      | LIST:data.listWalletAccounts.status     | NOT NULL |

