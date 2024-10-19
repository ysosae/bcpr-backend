@Middleware
Feature: Middleware - ListInstitutionStatus

  Background:
    Given Set facundo as main test user

  @Middleware
  Scenario: BCPRXRP-622-Get List Institutions of BanCOOP
    Given post a middleware request using endpoint v1/ListInstitutionStatus and body middleware/middlewareListInstitutionStatus.json
    Then I print out the results of the Middleware response
    Then The response code is 200
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/listInstitutionsOutput.json
    And I compare middleware response <Path> show the <Values>
      | traceId                   | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | LIST:data.institutionName | REGEX:^[a-z_ A-Z0-9-_\s\d]{1,60}$                                             |
      | LIST:data.institutionId   | REGEX:^[a-zA-Z0-9]{3,15}$                                                     |
      | LIST:data.object          | InstitutionStatus                                                             |

  Scenario: BCPRXRP-623-Get List Institutions (Invalid RouteId)
    Given post a middleware request using endpoint v1/ListInstitutionStatus with body middleware/middlewareListInstitutionStatus.json override table values
      | routeId | VPL-ERROR |
    Then I print out the results of the Middleware response
    Then Response code is either 500 or 422
    Then I validate Middleware response with Schema statement referenced at ./data/schemas/middleware/listInstitutionsOutput.json
    And I compare middleware response <Path> show the <Values>
      | traceId           | REGEX:^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$ |
      | errors[0].message | CONTAINS:OR:Validation error/Server not accesible.                            |



