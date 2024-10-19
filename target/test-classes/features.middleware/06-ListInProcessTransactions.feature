@Middleware
Feature: Middleware - ListInProcessTransactions


  Scenario: BCPRXRP-603-List In Process Transactions (Empty TRX)
    Given Set NoTrxPending as main test user
    Given post a middleware request using endpoint v1/ListInProcessTransactions with body middleware/middlewareListInProcessTransactionsALL.json override table values
      | institutionId  | 677                 |
      | customerId     | 031688579           |
      | accountId      | 0005440196770007238 |
      | customerIdType | SSN                 |
      | accountType    | CREDIT_CARD         |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId             | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:errors.message | SELECT EMPTY                                                                  |

  Scenario: BCPRXRP-604-List In Process Transactions (Type DEBIT)
    Given Set NoTrxPending as main test user
    Given post a middleware request using endpoint v1/ListInProcessTransactions with body middleware/middlewareListInProcessTransactions.json override table values
      | type           | DEBIT               |
      | institutionId  | 677                 |
      | customerId     | 031688579           |
      | accountId      | 0005440196770007238 |
      | customerIdType | SSN                 |
      | accountType    | CREDIT_CARD         |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId             | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:errors.message | SELECT EMPTY                                                                  |


  Scenario: BCPRXRP-605-List In Process Transactions (Type CREDIT)
    Given Set NoTrxPending as main test user
    Given post a middleware request using endpoint v1/ListInProcessTransactions with body middleware/middlewareListInProcessTransactions.json override table values
      | type           | CREDIT              |
      | institutionId  | 624                 |
      | customerId     | 031688579           |
      | accountId      | 0005440196240023229 |
      | customerIdType | SSN                 |
      | accountType    | CREDIT_CARD         |
    Then I print out the results of the Middleware response
    Then The response code is 400
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId             | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:errors.message | SELECT EMPTY                                                                  |

  Scenario: BCPRXRP-624-List In Process Transactions (Invalid)
    Given post a middleware request using endpoint v1/ListInProcessTransactions with body middleware/middlewareListInProcessTransactions.json override table values
      | type          | CREDIT              |
      | institutionId | 12332               |
      | customerId    | BB124232            |
      | accountId     | 0005440196770007238 |
      | accountType   | CREDIT_CARD         |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                |
      | errors[0].message | CONTAINS:OR:422 Validation error/Server 1 not accesible/INVALID INSTITUTION/ACCT NOT IN XREF |


  Scenario: BCPRXRP-602-List In Process Transactions (Exist TRX)
    Given Set yuliet as main test user
    Given post a middleware request using endpoint v1/ListInProcessTransactions and dynamic body middleware/middlewareListInProcessTransactionsALL.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-in-process-transactions-output.json
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
