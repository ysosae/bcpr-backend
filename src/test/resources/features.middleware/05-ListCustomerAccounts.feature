@Middleware
Feature: Middleware - ListCustomerAccounts

  Background:
    Given Set Admin as main test user

  Scenario: BCPRXRP-1040-Get List of Customer Accounts
    Given post a middleware request using endpoint v1/ListCustomerAccounts and dynamic body middleware/middlewareListCustomerAccounts.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-customer-accounts-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                  | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:data.object         | BaseAccount                                                                   |
      | LIST:data.accountId      | NOT NULL                                                                      |
      | LIST:data.lastFourDigits | REGEX:^\d{4}$                                                                 |
      | LIST:data.accountType    | OR:CREDIT_CARD-DEBIT_CARD                                                     |
      | LIST:data.accountStatus  | OR:ACTIVE-INACTIVE                                                            |
      | LIST:data.productName    | NOT NULL                                                                      |
      | LIST:data.country        | REGEX:^[A-Z]{2}$                                                              |
      | LIST:data.currency       | REGEX:^[A-Z]{3}$                                                              |
      #| LIST:data.balance                 | REGEX:^\d*\.?\d*$                                                              |


  Scenario: BCPRXRP-1041-Get List of Customer Accounts (Invalid CustomerId)
    Given post a middleware request using endpoint v1/ListCustomerAccounts with body middleware/middlewareListCustomerAccounts.json override table values
      | customerId | 3456456 |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/get-account-details-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                |
      | errors[0].message | CONTAINS:OR:422 Validation error/Server 1 not accesible/INVALID INSTITUTION/ACCT NOT IN XREF |



