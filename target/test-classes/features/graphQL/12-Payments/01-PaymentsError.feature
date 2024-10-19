@Graphql  @secondChance @Payments @IGNORE
Feature: GraphQL- Error Payments

  Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-1116-Make Payment Fail ACH - [EN]
    Given Override language to en
    Given post graphQL request with body graphQL/addWalletAccount.graphql override table values
      | accType | s         |
      | routing | 110000231 |
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/addWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.addWalletAccount.accountId  | NOT NULL |
      | data.addWalletAccount.expiration | NOT NULL |
      | data.addWalletAccount.linkToken  | NOT NULL |
      | data.addWalletAccount.requestId  | NOT NULL |
    Then save key accountIdPayments and path data.addWalletAccount.accountId as context variable
    Given post a graphQL request using graphQL/revalidateWalletAccount.graphql
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/revalidateWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.revalidateWalletAccount.accountId  | NOT NULL |
      | data.revalidateWalletAccount.expiration | NOT NULL |
      | data.revalidateWalletAccount.linkToken  | NOT NULL |
      | data.revalidateWalletAccount.requestId  | NOT NULL |
    Given post a graphQL request using graphQL/confirmWalletAccount.graphql
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/confirmWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.confirmWalletAccount.accountId | NOT NULL |
      | data.confirmWalletAccount.status    | NOT NULL |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    And I print size results with path data.listWalletAccounts of the response graphql
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId  | NOT NULL |
      | LIST:data.listWalletAccounts.routing    | NOT NULL |
     # | LIST:data.listWalletAccounts.accountNumber | NOT NULL |
      | LIST:data.listWalletAccounts.accType    | NOT NULL |
      | LIST:data.listWalletAccounts.customName | NOT NULL |
      | LIST:data.listWalletAccounts.status     | NOT NULL |
    Given post a graphQL request using graphQL/makePayment.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |
    Given Set Expression Attribute Values to query
      | String | :username  | nameUsername      |
      | String | :accountId | accountIdPayments |
    Given Set Expression Attribute Names to query
      | #username  | username  |
      | #accountId | accountId |
#    Given Retrieve data from scan table: Payment and filter: #username = :username and #accountId = :accountId save variables bellow
#      | status | String |
    Given Retrieve data from query table: Payment using this subId subId and status ERROR_PAYMENTS
    Then expected value ERROR_PAYMENTS:status into scenario data is equals SCENARIO_DATA:status
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
#    Then validate graphql record count data.findRecentPayments with DynamoDb records subId
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/findRecentPaymentsOutput.json
    And I compare response <Path> show the <Values>
      | LIST:data.findRecentPayments.lastFourDigits | NOT NULL |
#      | LIST:data.findRecentPayments.amount                  | NOT NULL                          |
      | LIST:data.findRecentPayments.date           | NOT NULL |
#      | LIST:data.findRecentPayments.status         | OR:CONFIRMED/PENDING_CONFIRMATION |
    #  | data.findRecentPayments[0].status           | CONTAINS:INVALID_FIELD_RECEIVED_ROUTINGNUMBER |

  Scenario: BCPRXRP-1117-Make Payment Fail ACH - [ES]
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
    Given post a graphQL request using graphQL/revalidateWalletAccount.graphql
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/revalidateWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.revalidateWalletAccount.accountId  | NOT NULL |
      | data.revalidateWalletAccount.expiration | NOT NULL |
      | data.revalidateWalletAccount.linkToken  | NOT NULL |
      | data.revalidateWalletAccount.requestId  | NOT NULL |
    Given post a graphQL request using graphQL/confirmWalletAccount.graphql
    And I print out the results of the response
    When The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/confirmWalletAccountOutput.json
    And I compare response <Path> show the <Values>
      | data.confirmWalletAccount.accountId | NOT NULL |
      | data.confirmWalletAccount.status    | NOT NULL |
    Given post a graphQL request using graphQL/getListWalletAccounts.graphql
    And I print out the results of the response
    And I print size results with path data.listWalletAccounts of the response graphql
    When The response is 200
   # Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listWalletAccountsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listWalletAccounts.accountId  | NOT NULL |
      | LIST:data.listWalletAccounts.routing    | NOT NULL |
    #  | LIST:data.listWalletAccounts.accountNumber | NOT NULL |
      | LIST:data.listWalletAccounts.accType    | NOT NULL |
      | LIST:data.listWalletAccounts.customName | NOT NULL |
      | LIST:data.listWalletAccounts.status     | NOT NULL |
    Given post a graphQL request using graphQL/makePayment.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/makePaymentOutput.json
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:No pudieron validarse los datos ingresados/¡Acceso denegado./Ha ocurrido un error inesperado |
    Given Set Expression Attribute Values to query
      | String | :username  | nameUsername      |
      | String | :accountId | accountIdPayments |
    Given Set Expression Attribute Names to query
      | #username  | username  |
      | #accountId | accountId |
#    Given Retrieve data from scan table: Payment and filter: #username = :username and #accountId = :accountId save variables bellow
#      | status | String |
    Given Retrieve data from query table: Payment using this subId subId and status ERROR_PAYMENTS
    Then expected value ERROR_PAYMENTS:status into scenario data is equals SCENARIO_DATA:status
    Given post a graphQL request using graphQL/findRecentPayments.graphql
    And I print out the results of the response
#    Then validate graphql record count data.findRecentPayments with DynamoDb records subId
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/findRecentPaymentsOutput.json
    And I compare response <Path> show the <Values>
      | LIST:data.findRecentPayments.lastFourDigits | NOT NULL |
#      | LIST:data.findRecentPayments.amount                  | NOT NULL                          |
      | LIST:data.findRecentPayments.date           | NOT NULL |
  #    | LIST:data.findRecentPayments.status         | OR:CONFIRMED/PENDING_CONFIRMATION |
#  | data.findRecentPayments[0].status           | CONTAINS:INVALID_FIELD_RECEIVED_ROUTINGNUMBER |
