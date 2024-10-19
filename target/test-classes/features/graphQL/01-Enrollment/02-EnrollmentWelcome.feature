@Graphql @notification
Feature:Enrollment welcome

  @Email
  @secondChance
  Scenario: BCPRXRP-459-Create User into Cognito
    Given Delete user Automation2023 from Cognito
    Given Set isUserCreation as true
   # Given filter attribute given_name with LUCY for validate test user
    #Given post a graphQL request using graphQL/getEnrollmentValidationYohara.graphql
    Given post a graphQL request using graphQL/getEnrollmentValidationLucy.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.enrollmentValidation.status       | CONTAINS:DATA_VALIDATED |
      | data.enrollmentValidation.expires      | NOT NULL                |
      | data.enrollmentValidation.enrollmentId | NOT NULL                |
    Then save response as context variable
    Then save key enrollmentId and path data.enrollmentValidation.enrollmentId as context variable
    Then Short wait between request
#    Given post a graphQL request using graphQL/getResendCode.graphql
    Given post graphQL request with body graphQL/getResendCode.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendCode.status       | CONTAINS:CODE_RESENT |
      | data.resendCode.enrollmentId | NOT NULL             |
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId |
      | String | :status_      | CODE_RESENT  |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
#    Given post a graphQL request using graphQL/getCodeValidation.graphql
    Given post graphQL request with body graphQL/getCodeValidation.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | phoneCode    | SCENARIO_DATA:phoneCode    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |
    Given post graphQL request with body graphQL/getEnrollUser.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | username     | Automation2023             |
      | password     | newPass                    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.enrollUser.status       | CONTAINS:USER_CREATED |
      | data.enrollUser.enrollmentId | NOT NULL              |
    And Wait 5 seconds between request
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

  @Email
  Scenario: BCPRXRP-326-Update password into Enrolment
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023        |
      | PASSWORD | SCENARIO_DATA:newPass |
    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
      | currentPassword | SCENARIO_DATA:newPass       |
      | newPassword     | SCENARIO_DATA:newPassUpdate |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023              |
      | PASSWORD | SCENARIO_DATA:newPassUpdate |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user Automation2023


  @secondChance
  Scenario: BCPRXRP-567-Sending duplicate enrollment with the same SSN
    Given Override language to en
    Given post a graphQL request using graphQL/getEnrollmentValidationLucy.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |