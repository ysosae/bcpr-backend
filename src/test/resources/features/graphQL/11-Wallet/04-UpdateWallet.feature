@Graphql @secondChance @crt
Feature: GraphQL-Update Wallets

   Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-486-Update custome Name Wallet Account in status PENDING_CONFIRMATION
    Given Override language to en
   # Given post a graphQL request using graphQL/addWalletAccount.graphql
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | routing       | 110000000        |
      | accountNumber | 1111222233330000 |
      | accType       | s                |
      | customName    | QA-Wallet        |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | status | accountId | SCENARIO_DATA:accountIdPayments | PENDING_CONFIRMATION |
    Given post graphQL request with body graphQL/updateWalletCustomName.graphql override table values
      | accountId  | SCENARIO_DATA:accountIdPayments |
      | customName | QAUpdateTest                    |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.updateWalletCustomName.accountId | SCENARIO_DATA:accountIdPayments |
      | data.updateWalletCustomName.status    | CONTAINS:UPDATED                |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | customName | accountId | SCENARIO_DATA:accountIdPayments | QAUpdateTest |


  Scenario: BCPRXRP-487-Update custome Name Wallet Account in status CONFIRMED
    Given Override language to en
    #Given post a graphQL request using graphQL/addWalletAccount.graphql
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | routing       | 110000000        |
      | accountNumber | 1111222233330000 |
      | accType       | s                |
      | customName    | QA-Wallet        |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
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
    Given post a graphQL request using graphQL/confirmWalletAccount.graphql
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/confirmWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.confirmWalletAccount.accountId      | NOT NULL |
      | data.confirmWalletAccount.status         | NOT NULL |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | status | accountId | SCENARIO_DATA:accountIdPayments | CONFIRMED |
    Given post graphQL request with body graphQL/updateWalletCustomName.graphql override table values
      | accountId  | SCENARIO_DATA:accountIdPayments |
      | customName | QAUpdateTest                    |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.updateWalletCustomName.accountId | SCENARIO_DATA:accountIdPayments |
      | data.updateWalletCustomName.status    | CONTAINS:UPDATED                |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | customName | accountId | SCENARIO_DATA:accountIdPayments | QAUpdateTest |
