@Graphql @IGNORE
Feature: RegainAccess recover blocked user

  @IGNORE
  Scenario: BCPRXRP-855 - RegainAccess recover blocked user
    Given post a graphQL request using graphQL/getRegainAccessValidationYohara.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessValidation.status         | CONTAINS:DATA_VALIDATED |
      | data.regainAccessValidation.expires        | NOT NULL                |
      | data.regainAccessValidation.regainAccessId | NOT NULL                |
    Then save response as context variable
    Then save key regainAccessId and path data.regainAccessValidation.regainAccessId as context variable
    Given post a graphQL request using graphQL/getResendRegainAccessCode.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendRegainAccessCode.status         | CONTAINS:CODE_RESENT |
      | data.resendRegainAccessCode.regainAccessId | NOT NULL             |
    Given Set Expression Attribute Values to query
      | String | :ssn            | 145540014      |
      | String | :status_        | CODE_RESENT     |
      | String | :expirationCode | Today           |
      | String | :type_          | SSN             |
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
    Given post graphQL request with body graphQL/getSetNewPassword.graphql override table values
      | password | Test12345 |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | yoharasantana     |
      | PASSWORD | Test12345         |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |


  Scenario: BCPRXRP-587 - Recover User and Password
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
    Given post a graphQL request using graphQL/getResendRegainAccessCode.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendRegainAccessCode.status         | CONTAINS:CODE_RESENT |
      | data.resendRegainAccessCode.regainAccessId | NOT NULL             |
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
    Given post graphQL request with body graphQL/getSetNewPassword.graphql override table values
      | password | newPass |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |
    Given Set isUserCreation as false
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023        |
      | PASSWORD | SCENARIO_DATA:newPass |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user Automation2023

  Scenario: BCPRXRP-673-Validate SMS Token (Regain Access)
    Given Set isUserCreation as true
    Given post a graphQL request using graphQL/getRegainAccessValidationLucy.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessValidation.status         | CONTAINS:DATA_VALIDATED |
      | data.regainAccessValidation.expires        | NOT NULL                |
      | data.regainAccessValidation.regainAccessId | NOT NULL                |
    Then save response as context variable
    Then save key regainAccessId and path data.regainAccessValidation.regainAccessId as context variable
    Given Set Expression Attribute Values to query
      | String | :regainAccessId | regainAccessId |
      | String | :status_        | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: RegainAccessData and filter: regainAccessId = :regainAccessId and #status = :status_ save variables bellow
      | socialSecurityNumber | String |
      | phoneCode            | Number |
      | regainAccessId       | String |
    Given post a graphQL request using graphQL/getRegainAccessCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessCodeValidation.status   | CONTAINS:USER_RECOVERED |
      | data.regainAccessCodeValidation.username | NOT NULL                |