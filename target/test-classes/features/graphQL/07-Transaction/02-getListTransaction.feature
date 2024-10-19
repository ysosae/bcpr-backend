@Graphql @secondChance @crt
Feature: List Transactions

  Scenario: BCPRXRP-282 get List of Transactions
    Given Set TrxProcessed as main test user
    Given post a graphQL request using graphQL/listTransactionsWithSelectedDate.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listTransactions.offset                     | NUMBER   |
      | data.listTransactions.quantity                   | NUMBER   |
      | LIST:data.listTransactions.transactions.id       | NOT NULL |
      | LIST:data.listTransactions.transactions.currency | NOT NULL |
      | LIST:data.listTransactions.transactions.date     | NOT NULL |


  Scenario: BCPRXRP-75-List Transactions by current period
    Given Set TrxProcessed as main test user
    Given post graphQL request with body graphQL/listTransactions.graphql override table values
      | fromDate | currentFromDate |
      | toDate   | currentToDate   |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listTransactions.offset                     | NUMBER   |
      | data.listTransactions.quantity                   | NUMBER   |
      | LIST:data.listTransactions.transactions.id       | NOT NULL |
      | LIST:data.listTransactions.transactions.currency | NOT NULL |
      | LIST:data.listTransactions.transactions.date     | NOT NULL |
# If not Exist Transaction for current period
#    Then I compare response <Path> show the <Values>
#      | LIST:errors.message | Not found |

  @IGNORE
  Scenario: BCPRXRP-720-List Transactions process Pagination with more than 20 transaction
    Given Set TrxProcessed as main test user
    Given post graphQL request with body graphQL/listTransactionsPagination.graphql override table values
      | limit  | 20 |
      | offset | 0  |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listTransactions.offset                     | NUMBER   |
      | data.listTransactions.quantity                   | NUMBER   |
      | LIST:data.listTransactions.transactions.id       | NOT NULL |
      | LIST:data.listTransactions.transactions.currency | NOT NULL |
      | LIST:data.listTransactions.transactions.date     | NOT NULL |
    Then save key offset and path data.listTransactions.offset  as context variable
    Given post graphQL request with body graphQL/listTransactionsPagination.graphql override table values
      | limit    | 20                   |
      | offset   | SCENARIO_DATA:offset |
      | fromDate | 2023-01-05           |
      | toDate   | 2023-02-05           |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.listTransactions.offset                     | NUMBER   |
      | data.listTransactions.quantity                   | NUMBER   |
      | LIST:data.listTransactions.transactions.id       | NOT NULL |
      | LIST:data.listTransactions.transactions.currency | NOT NULL |
      | LIST:data.listTransactions.transactions.date     | NOT NULL |
    Then path data.listTransactions.offset and save value as offset

  @IGNORE
  Scenario: BCPRXRP-285 List Transactions (EMPTY)
    Given Set NoTrxProcessed as main test user
    Given Override language to en
    Given post a graphQL request using graphQL/listTransactions.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | Not found |