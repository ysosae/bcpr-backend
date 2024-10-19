@Middleware
Feature: Middleware - GetTransaction

  Background:
    Given Set TrxProcessed as main test user
    Given post a middleware request using endpoint v1/ListTransactions and dynamic body middleware/middlewareListTransactionsALL.json
    Then Select from data.transactions[0].id and save its id value as transactionId

  Scenario: BCPRXRP-655-Get Transactions (Only one)
    Given post a middleware request using endpoint v1/GetTransaction and dynamic body middleware/middlewareGetTrxCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    #Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/get-transaction-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId          | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.object      | Transaction                                                                   |
      | data.type        | OR:CREDIT/DEBIT                                                               |
      | data.amount      | NOT NULL                                                                      |
      | data.description | NOT NULL                                                                      |
      #| data.country     | REGEX:^[A-Z]{2}$                                                              |
      | data.currency    | REGEX:^[A-Z]{3}$                                                              |
      | data.date        | REGEX:^\d{4}-\d{2}-\d{2}$                                                     |


  Scenario: BCPRXRP-656-Get Transactions (ERROR)
    Given post a middleware request using endpoint v1/GetTransaction with body middleware/middlewareGetTrxCommons.json override table values
      | institutionId | B5642232                |
      | transactionId | MT221090105000010000012 |
      | customerId    | 598226982               |
      | accountId     | 0004199100010029958     |
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/get-transaction-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                |
      | errors[0].message | CONTAINS:OR:422 Validation error/Server 1 not accesible/INVALID INSTITUTION/ACCT NOT IN XREF |