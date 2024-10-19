@Graphql @IGNORE
Feature: Regain Access.

  Scenario: BCPRXRP-287-Get RegainAccess Validation
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023 |
      | PASSWORD | Test847956**   |
    Given post a graphQL request using graphQL/getRegainAccessValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessValidation.status         | CONTAINS:DATA_VALIDATED |
      | data.regainAccessValidation.expires        | NOT NULL                |
      | data.regainAccessValidation.regainAccessId | NOT NULL                |
    Then save response as context variable
    Then save key regainAccessId and path data.regainAccessValidation.regainAccessId as context variable


  Scenario: BCPRXRP-288-Get getResendCode into Regain Access
    Given post a graphQL request using graphQL/getResendRegainAccessCode.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendRegainAccessCode.status         | CONTAINS:CODE_RESENT |
      | data.resendRegainAccessCode.regainAccessId | NOT NULL             |

  Scenario: BCPRXRP-289-Get CodeValidation into Regain Access
    Given Set Expression Attribute Values to query
      | String | :ssn            | customerId  |
      | String | :status_        | CODE_RESENT |
      | String | :expirationCode | Today       |
      | String | :type_          | SSN         |
    Given Set Expression Attribute Names to query
      | #status | status |
      | #type   | type   |
    Given Retrieve data from scan table: RegainAccessData and filter: socialSecurityNumber = :ssn and #status = :status_ and codeExpiration >= :expirationCode and #type >= :type_ save variables bellow
      | socialSecurityNumber | String |
      | phoneCode            | Number |
      | regainAccessId       | String |
    Given post a graphQL request using graphQL/getRegainAccessCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessCodeValidation.status   | CONTAINS:USER_RECOVERED |
      | data.regainAccessCodeValidation.username | NOT NULL                |

  Scenario: BCPRXRP-290-Set NewPassword into Recover Access
    Given post graphQL request with body graphQL/getSetNewPassword.graphql override table values
      | password | Automation2023 |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |





