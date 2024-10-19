@Graphql @IGNORE
Feature:Enrollment.

  Scenario: BCPRXRP-281-Get Enrolment Validation
    Given post a graphQL request using graphQL/getEnrollmentValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.enrollmentValidation.status       | CONTAINS:DATA_VALIDATED |
      | data.enrollmentValidation.expires      | NOT NULL                |
      | data.enrollmentValidation.enrollmentId | NOT NULL                |
    Then save response as context variable
    Then save key enrollmentId and path data.enrollmentValidation.enrollmentId as context variable


  Scenario: BCPRXRP-283-Get getResendCode into Enrolment
    Given post a graphQL request using graphQL/getResendCode.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendCode.status       | CONTAINS:CODE_RESENT |
      | data.resendCode.enrollmentId | NOT NULL             |


  Scenario: BCPRXRP-284-Get CodeValidation into Enrolment
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId       |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Given post a graphQL request using graphQL/getCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |


  Scenario: BCPRXRP-286-Create EnrollUser into Enrolment
    Given post a graphQL request using graphQL/getEnrollUserRandom.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.enrollUser.status       | CONTAINS:USER_CREATED |
      | data.enrollUser.enrollmentId | NOT NULL              |


  Scenario: BCPRXRP-325-BCPRXRP-561-Update password into Enrolment and Settings
    Given Set Automation as main test user
    Given perform login using data in graphQL/login.graphql
    Given post a graphQL request using graphQL/getPasswordUpdate.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
    Given perform login using data in graphQL/login.graphql

  Scenario: BCPRXRP-460-Verify a subId field in the BCPR-ENV-EnrollmentData table
    Given Set yalithza as main test user
    And save cognito attribute sub by username as context variable
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId |
      | String | :status_      | USER_CREATED  |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | subId        | String |
      | enrollmentId | String |

