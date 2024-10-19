@Middleware
Feature: Middleware - GetAccountDetails

  Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-1038-Get Account Details - (8456 - Yuliet)
    Given Set yuliet as main test user
    Given post a middleware request using endpoint v1/GetAccountDetails and body middleware/middlewareGetCustomerInformationCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/get-account-details-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                      | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.object                  | CreditCardDetails                                                             |
      | data.availableCredit         | REGEX:^\d*\.?\d*$                                                             |
      | data.currentBalance          | REGEX:^\d*\.?\d*$                                                             |
      | data.creditLimit             | REGEX:^\d*\.?\d*$                                                             |
      | data.minimumPaymentDueAmount | REGEX:^\d*\.?\d*$                                                             |
      | data.cardNumber              | NOT NULL                                                                      |
      | data.cardEmbossedName        | NOT NULL                                                                      |
      | data.rewards                 | NOT NULL                                                                      |
      | data.rewardsBalance          | NOT NULL                                                                      |
      | data.cardStatus              | OR:ACTIVE-INACTIVE                                                            |
      | data.blockingStatus          | OR:BCPRBTMP-BCPRBFRD-BCPRBBLK-EMPTY                                           |
      | data.paymentDueDate          | REGEX:^\d{4}-\d{2}-\d{2}$                                                     |
      | data.currentFromDate         | REGEX:^\d{4}-\d{2}-\d{2}$                                                     |
#      | data.previousFromDate             | REGEX:^\d{4}-\d{2}-\d{2}$                                                      |
#      | data.previousToDate               | REGEX:^\d{4}-\d{2}-\d{2}$                                                      |
#      | data.lastFromDate                 | REGEX:^\d{4}-\d{2}-\d{2}$                                                      |
#      | data.lastToDate                   | REGEX:^\d{4}-\d{2}-\d{2}$                                                      |
      | data.cardExpirationDate      | REGEX:^\d{4}-\d{2}$                                                           |
      | data.postalCode              | REGEX:^\d{5}$                                                                 |

  Scenario: BCPRXRP-1037-Get Account Details (Invalid CustomerId)
    Given post a middleware request using endpoint v1/GetAccountDetails with body middleware/middlewareGetCustomerInformationCommons.json override table values
      | customerId    | 3456456117 |
      | institutionId | 123        |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/get-account-details-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                 |
      | errors[0].message | CONTAINS:OR:422 Validation error/Unprocessable Entity/Server 1 not accesible/ACCT NOT IN XREF |




