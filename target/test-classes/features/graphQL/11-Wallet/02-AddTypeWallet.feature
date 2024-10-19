@Graphql @Payments @secondChance @notification @crt
Feature: GraphQL-Select Saving/Checking account

  Description: Select "saving/checking" account and validate that the account created is of the same type.

  Background:
    Given Set facundo as main test user

  @Email
  Scenario: BCPRXRP-274-Add Wallet Saving Account
    Given Override language to es
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | accType    | s                |
      | customName | QA-Wallet-Saving |
    And I print out the results of the response
    When The response is 200
   # Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
   # And I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId | SCENARIO_DATA:accountIdPayments |
      | data.listWalletAccounts.routing        | NOT NULL                        |
      | data.listWalletAccounts.accountNumber  | NOT NULL                        |
      | data.listWalletAccounts.status         | NOT NULL                        |
    Then in path data.listWalletAccounts search for accountId that contain SCENARIO_DATA:accountIdPayments and validate following
      | accType    | s                |
      | customName | QA-Wallet-Saving |

   @Email
  Scenario: BCPRXRP-275-Add Wallet Checking Account
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | accType    | W                  |
      | customName | QA-Wallet-Checking |
    And I print out the results of the response
    When The response is 200
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
   # Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId | SCENARIO_DATA:accountIdPayments |
      | data.listWalletAccounts.routing        | NOT NULL                        |
      | data.listWalletAccounts.accountNumber  | NOT NULL                        |
      | data.listWalletAccounts.status         | NOT NULL                        |
    Then in path data.listWalletAccounts search for accountId that contain SCENARIO_DATA:accountIdPayments and validate following
      | accType    | W                  |
      | customName | QA-Wallet-Checking |