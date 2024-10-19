@Middleware
Feature: Middleware - ListTransactions

  Scenario: BCPRXRP-1031-List Transactions (EMPTY)
    Given Set NoTrxProcessed as main test user
    Given post a middleware request using endpoint v1/ListTransactions with body middleware/middlewareListTransactionsALL.json override table values
      | institutionId  | 636                 |
      | customerId     | 598035111           |
      | accountId      | 0005440206360003321 |
      | customerIdType | SSN                 |
      | accountType    | CREDIT_CARD         |
      | fromDate       | 2022-07-01          |
      | toDate         | 2022-08-12          |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-transactions-output.json
    Then I compare response <Path> show the <Values>
      | errors[0].message | SELECT EMPTY |

  Scenario: BCPRXRP-625-List Transactions (ALL)
    Given Set yuliet as main test user
    Given post a middleware request using endpoint v1/ListTransactions and dynamic body middleware/middlewareListTransactionsALL.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                            | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.offset                        | NUMBER                                                                        |
      | data.quantity                      | NUMBER                                                                        |
      | LIST:data.transactions.object      | Transaction                                                                   |
      | LIST:data.transactions.id          | NOT NULL                                                                      |
      | LIST:data.transactions.type        | OR:DEBIT/CREDIT                                                               |
#      | LIST:data.transactions.amount                      | REGEX:^\d*\.?\d*$                                                              |
#      | LIST:data.transactions.country                     | REGEX:^[A-Z]{2}$                                                              |
      | LIST:data.transactions.currency    | REGEX:^[A-Z]{3}$                                                              |
      | LIST:data.transactions.date        | NOT NULL                                                                      |
      | LIST:data.transactions.description | NOT NULL                                                                      |
#      | LIST:data.transactions.merchantCategoryCode        | NOT NULL                                                                      |
#      | LIST:data.transactions.merchantCategoryDescription | NOT NULL                                                                      |

  Scenario: BCPRXRP-626-List Transactions (DEBIT)
    Given Set yuliet as main test user
    Given post a middleware request using endpoint v1/ListTransactions and dynamic body middleware/middlewareListTransactionsTypeDEBIT.json
    Then I print out the results of the Middleware response
    Then Response code is either 200 or 400
    Given if not Exist Transaction for current period
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                            | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:data.transactions.type        | OR:CREDIT-DEBIT                                                               |
      | LIST:data.transactions.currency    | REGEX:^[A-Z]{3}$                                                              |
      | LIST:data.transactions.date        | REGEX:^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$                                   |
      | LIST:data.transactions.description | NOT NULL                                                                      |
      #| LIST:data.transactions.merchantCategoryDescription| NOT NULL                                                                       |
      #| LIST:data.transactions.merchantCategoryCode    | NUMBER                                                                         |


  Scenario: BCPRXRP-627-List Transactions (CREDIT)
    Given Set yuliet as main test user
    Given post a middleware request using endpoint v1/ListTransactions and dynamic body middleware/middlewareListTransactionsTypeCREDIT.json
    Then I print out the results of the Middleware response
    Then Response code is either 200 or 400
    Given if not Exist Transaction for current period
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                            | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:data.transactions.type        | OR:CREDIT-DEBIT                                                               |
      | LIST:data.transactions.currency    | REGEX:^[A-Z]{3}$                                                              |
      | LIST:data.transactions.date        | REGEX:^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$                                   |
      | LIST:data.transactions.description | NOT NULL                                                                      |
      #| LIST:data.transactions.merchantCategoryDescription| NOT NULL                                                                       |
      #| LIST:data.transactions.merchantCategoryCode    | NUMBER                                                                         |

  Scenario: BCPRXRP-628-List Transactions (Invalid)
    Given Set TrxProcessed as main test user
    Given post a middleware request using endpoint v1/ListTransactions with body middleware/middlewareListTransactionsALL.json override table values
      | institutionId | BB01232424       |
      | customerId    | 3456456117       |
      | accountId     | 4000456275129173 |
      | limit         | 20               |
      | offset        | 10               |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                |
      | errors[0].message | CONTAINS:OR:422 Validation error/Server 1 not accesible/INVALID INSTITUTION/ACCT NOT IN XREF |
