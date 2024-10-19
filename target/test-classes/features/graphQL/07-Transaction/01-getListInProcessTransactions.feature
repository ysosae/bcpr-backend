@Graphql @secondChance @crt
Feature: In Process Transactions

  @IGNORE
  Scenario: BCPRXRP-285 get List in Process Transactions (EMPTY)
    Given Set NoTrxPending as main test user
    Given Override language to en
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | Not found |


  Scenario: BCPRXRP-468 get List in Process Transactions description name in transactions when is empty field EN
    Given Set TrxPending as main test user
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                       |
      | data.listInProcessTransactions.quantity                      | NUMBER                       |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL                     |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                       |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL                     |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:Payment in commerce |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT              |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL                     |


  Scenario: BCPRXRP-469 get List in Process Transactions description name in transactions when is empty field ES
    Given Set TrxPending as main test user
    Given Override language to es
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                 |
      | data.listInProcessTransactions.quantity                      | NUMBER                 |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL               |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                 |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL               |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:RETAIL STORES |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT        |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL               |


  Scenario: BCPRXRP-647 get List in Process Transactions description name in transactions when is empty field RU
    Given Set TrxPending as main test user
    Given Override language to es
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                 |
      | data.listInProcessTransactions.quantity                      | NUMBER                 |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL               |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                 |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL               |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:RETAIL STORES |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT        |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL               |

#  @IGNORE
  Scenario: BCPRXRP-719 get List in Process Transactions Pagination with more than 20 transaction
    #Given Set yalithza as main test user
    Given Set TrxPending as main test user
    Given Override language to en
    #Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    Given post graphQL request with body graphQL/listInProcessTransactionsPagination.graphql override table values
      | limit  | 20 |
      | offset | 0  |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                 |
      | data.listInProcessTransactions.quantity                      | NUMBER                 |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL               |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                 |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL               |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:RETAIL STORES |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT        |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL               |
    Then path data.listInProcessTransactions.offset and save value as offset
    Given post graphQL request with body graphQL/listInProcessTransactionsPagination.graphql override table values
      | limit  | 20                   |
      | offset | SCENARIO_DATA:offset |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                 |
      | data.listInProcessTransactions.quantity                      | NUMBER                 |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL               |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                 |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL               |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:RETAIL STORES |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT        |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL               |
    Then path data.listInProcessTransactions.offset and save value as offset

  @IGNORE
  Scenario: BCPRXRP-504 List in Process Transactions description name Spanish when language is distinct RU
    Given Set TrxPending as main test user
    Given Override language to ru
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                 |
      | data.listInProcessTransactions.quantity                      | NUMBER                 |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL               |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                 |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL               |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:RETAIL STORES |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT        |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL               |

  @IGNORE
  Scenario: BCPRXRP-437 Return List of InProgress transactions with memoPosted status
    Given Set TrxPending as main test user
    Given post a graphQL request using graphQL/listInProcessTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listInProcessTransactions.offset                        | NUMBER                       |
      | data.listInProcessTransactions.quantity                      | NUMBER                       |
      | LIST:data.listInProcessTransactions.transactions.id          | NOT NULL                     |
#      | LIST:data.listInProcessTransactions.transactions.amount      | AMOUNT                       |
      | LIST:data.listInProcessTransactions.transactions.currency    | NOT NULL                     |
      | LIST:data.listInProcessTransactions.transactions.description | CONTAINS:Payment in commerce |
      | LIST:data.listInProcessTransactions.transactions.type        | OR:DEBIT/CREDIT              |
      | LIST:data.listInProcessTransactions.transactions.date        | NOT NULL                     |