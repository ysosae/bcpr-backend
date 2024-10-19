@Middleware
Feature: Middleware - InstitutionProfile

  Background:
    Given Set facundo as main test user

  Scenario: BCPRXRP-653-Get Institution Profile (636)
    Given post a middleware request using endpoint v1/InstitutionsProfile and dynamic body middleware/middlewareInstitutionCommons.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    And I compare middleware response <Path> show the <Values>
      | traceId                        | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | data.object                    | InstitutionProfile                                                            |
      | data.institutionName           | NOT NULL                                                                      |
      | data.institutionId             | REGEX:^[a-zA-Z0-9]{3,15}$                                                     |
      | LIST:data.addresses.address1   | NOT NULL                                                                      |
      | LIST:data.addresses.city       | NOT NULL                                                                      |
      | LIST:data.addresses.country    | NOT NULL                                                                      |
      | LIST:data.addresses.country    | REGEX:^[A-Z]{2}$                                                              |
      | LIST:data.addresses.postalCode | REGEX:^\d{5}$                                                                 |


  Scenario: BCPRXRP-654-Get Institution Profile (ERROR)
    Given post a middleware request using endpoint v1/InstitutionsProfile with body middleware/middlewareInstitutionCommons.json override table values
      | institutionId | 111 |
    Then I print out the results of the Middleware response
    Then The response code is 422
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$                |
      | errors[0].message | CONTAINS:OR:422 Validation error/Server 1 not accesible/INVALID INSTITUTION/ACCT NOT IN XREF |
