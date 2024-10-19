@Graphql @crt
Feature: GraphQL-Mark as Delete Wallet

  Description: workflows adding wallets and delete

  Scenario: BCPRXRP-267-Create-Validate and Mark as Delete Wallet By Mutation - Confirmed
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | routing       | 110000000        |
      | accountNumber | 1111222233330000 |
      | accType       | s                |
      | customName    | QA-Wallet-Delete |
    And I print out the results of the response
    When The response is 200
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    #Given post a graphQL request using graphQL/revalidateWalletAccount.graphql
    Given post graphQL request with body graphQL/revalidateWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    Given post graphQL request with body graphQL/confirmWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | status | accountId | SCENARIO_DATA:accountIdPayments | CONFIRMED |
    Given post a graphQL request using graphQL/markWalletAccountAsDeleted.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.markWalletAccountAsDeleted.accountId | SCENARIO_DATA:accountIdPayments |
      | data.markWalletAccountAsDeleted.status    | DELETED                         |

 Scenario: BCPRXRP-268-Create-Validate and Mark as Delete Wallet By Mutation - PENDING_CONFIRMATION
   Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | routing       | 110000000        |
      | accountNumber | 1111222233330000 |
      | accType       | s                |
      | customName    | QA-Wallet-Delete |
    And I print out the results of the response
    When The response is 200
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | status | accountId | SCENARIO_DATA:accountIdPayments | PENDING_CONFIRMATION |
    Given post graphQL request with body graphQL/markWalletAccountAsDeleted.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.markWalletAccountAsDeleted.accountId | SCENARIO_DATA:accountIdPayments |
      | data.markWalletAccountAsDeleted.status    | DELETED                         |

 Scenario: BCPRXRP-269-Make Payment with Deleted Wallet
    Given Override language to es
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | accType | W        |
      | routing | 89123205 |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post graphQL request with body graphQL/revalidateWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/revalidateWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.revalidateWalletAccount.accountId  | NOT NULL |
      | data.revalidateWalletAccount.expiration | NOT NULL |
      | data.revalidateWalletAccount.linkToken  | NOT NULL |
      | data.revalidateWalletAccount.requestId  | NOT NULL |
    Given post graphQL request with body graphQL/confirmWalletAccount.graphql override table values
      | accountId | SCENARIO_DATA:accountIdPayments |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.confirmWalletAccount.accountId | NOT NULL |
      | data.confirmWalletAccount.status    | NOT NULL |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    And I print size results with path data.listWalletAccounts of the response graphql
    When The response is 200
    Then from PATH get key value: KEY from item that contains CONTAINS_KEY with value WITH_VALUE expected value: EXPECTED
      | data.listWalletAccounts | status | accountId | SCENARIO_DATA:accountIdPayments | CONFIRMED |
    Given post a graphQL request using graphQL/markWalletAccountAsDeleted.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.markWalletAccountAsDeleted.accountId | SCENARIO_DATA:accountIdPayments |
      | data.markWalletAccountAsDeleted.status    | DELETED                         |
    Given post a graphQL request using graphQL/makePayment.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:No pudieron validarse los datos ingresados/¡Acceso denegado./Ha ocurrido un error inesperado |
    Given Set Expression Attribute Values to query
      | String | :username  | nameUsername      |
      | String | :accountId | accountIdPayments |
    Given Set Expression Attribute Names to query
      | #username  | username  |
      | #accountId | accountId |
    Given Retrieve data from query table: Payment using this subId subId and status ERROR_PAYMENTS
    Then expected value ERROR_PAYMENTS:status into scenario data is equals SCENARIO_DATA:status
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
    And I compare response <Path> show the <Values>
      | LIST:data.findRecentPayments.lastFourDigits | NOT NULL |
#      | LIST:data.findRecentPayments.amount                  | NOT NULL                          |
      | LIST:data.findRecentPayments.date           | NOT NULL |
  #    | LIST:data.findRecentPayments.status         | OR:CONFIRMED/PENDING_CONFIRMATION |
#  | data.findRecentPayments[0].status           | CONTAINS:INVALID_FIELD_RECEIVED_ROUTINGNUMBER |