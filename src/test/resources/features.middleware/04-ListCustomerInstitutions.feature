@Middleware
Feature: Middleware - ListCustomerInstitutions

  Background:
    Given Set admin as main test user

  Scenario: BCPRXRP-600-List Customer Institutions
    Given post a middleware request using endpoint v1/ListCustomerInstitutions and dynamic body middleware/middlewareListCustomerInstitutions.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-customer-institutions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId                   | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:data.object          | Institution                                                                   |
      | LIST:data.institutionId   | REGEX:^[a-zA-Z0-9]{3,15}$                                                     |
      | LIST:data.institutionName | REGEX:^[a-z_ A-Z0-9-_\s\d]{1,60}$                                             |


  Scenario: BCPRXRP-619-List Customer Institutions (Invalid CustomerId)
    Given post a middleware request using endpoint v1/ListCustomerInstitutions with body middleware/middlewareListCustomerInstitutions.json override table values
      | customerId | B5642232 |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/list-customer-institutions-output.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | CONTAINS:OR:422 Validation error/ACCT NOT IN XREF                             |