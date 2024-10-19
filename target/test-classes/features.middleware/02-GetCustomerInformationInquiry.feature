@Middleware
Feature: Middleware - GetCustomerInformationInquiry

  Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-1039-Get Customer Institution list from logged user
    Given post a middleware request using endpoint v1/GetCustomerInformationInquiry and dynamic body middleware/middlewareGetCustomerInformationInquiry.json
    Then I print out the results of the Middleware response
    Then The response code is 200
   # Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/getCustomerInformationInquiryOutput.json
    And I compare middleware response <Path> show the <Values>
      | traceId                        | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.object                    | Customer                                                                      |
      | data.dateOfBirth               | REGEX:^\d{4}-\d{2}-\d{2}$                                                     |
      | data.phoneNumbers[0]           | NOT NULL                                                                      |
      | data.phoneNumbers[1]           | REGEX:^\d{3}-\d{3}-\d{4}$                                                     |
      | data.phoneNumbers[2]           | NOT NULL                                                                      |
      | data.emails[0]                 | EMAIL                                                                         |
      | data.firstName                 | REGEX:^[a-zA-Z0-9 -]{0,64}$                                                   |
      | data.middleName                | NOT NULL                                                                      |
      | data.lastName                  | REGEX:^[a-zA-Z0-9 -]{0,64}$                                                   |
      | LIST:data.addresses.address1   | NOT NULL                                                                      |
      | LIST:data.addresses.address2   | NOT NULL                                                                      |
      | LIST:data.addresses.city       | NOT NULL                                                                      |
      | LIST:data.addresses.state      | NOT NULL                                                                      |
      | LIST:data.addresses.country    | NOT NULL                                                                      |
      | LIST:data.addresses.country    | REGEX:^[A-Z]{2}$                                                              |
      | LIST:data.addresses.postalCode | REGEX:^\d{5}$                                                                 |

  Scenario: BCPRXRP-1036-Get Customer Institution list (Invalid CustomerId)
    Given post a middleware request using endpoint v1/GetCustomerInformationInquiry with body middleware/middlewareGetCustomerInformationInquiry.json override table values
      | customerId    | 012440989  |
      | institutionId | BB01232424 |
    Then I print out the results of the Middleware response
    Then The response code is 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/getCustomerInformationInquiryOutput.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | CONTAINS:OR:422 Validation error/INVALID INSTITUTION/Server 1 not accesible   |




