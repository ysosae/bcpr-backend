@Graphql @PasswordPolicy
Feature:Enrollment - Password Policy

  Scenario: BCPRXRP-919 Create User into Cognito using wrong pass policy
    Given Delete user Automation2023 from Cognito
    #And delete ALL item dynamoDb by table name LastPassword and item name Automation2023
    Given Set isUserCreation as true
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
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId   |
      | String | :status_      | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Given post graphQL request with body graphQL/getCodeValidation.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | phoneCode    | SCENARIO_DATA:phoneCode    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |
    Given I set the new Password with wrong police requirements with body graphQL/getEnrollUser.graphql
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | username     | Automation2023             |
      | password     | newPassUpdate              |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: restore password policy configuration (1)
    Given restore password policy configuration

  Scenario: BCPRXRP-929 Create User into Cognito using password including the username
    Given Delete user Automation2023 from Cognito
    Given Set isUserCreation as true
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
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId   |
      | String | :status_      | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Given post graphQL request with body graphQL/getCodeValidation.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | phoneCode    | SCENARIO_DATA:phoneCode    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |
    Given I set the new Password with wrong police requirements with body graphQL/getEnrollUserWithUsername.graphql
      | enrollmentId | SCENARIO_DATA:enrollmentId        |
      | username     | Automation2023                    |
      | password     | SCENARIO_DATA:newPassWithUsername |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password must not contain the username |

  Scenario: restore password policy configuration (2)
    Given restore password policy configuration

  Scenario: BCPRXRP-930 Create User into Cognito using password including a blackListed character
    Given Delete user Automation2023 from Cognito
    Given Set isUserCreation as true
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
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId   |
      | String | :status_      | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Given post graphQL request with body graphQL/getCodeValidation.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | phoneCode    | SCENARIO_DATA:phoneCode    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |
    Given I set the new Password with wrong police requirements with body graphQL/getEnrollUserWithUsernameWithBlackListed.graphql
      | enrollmentId | SCENARIO_DATA:enrollmentId           |
      | username     | Automation2023                       |
      | password     | SCENARIO_DATA:newPassWithBlackListed |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: restore password policy configuration (3)
    Given restore password policy configuration

  Scenario: BCPRXRP-1058 Create User into Cognito but his password is too short
    Given Delete user Automation2023 from Cognito
    Given Set isUserCreation as true
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
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId   |
      | String | :status_      | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Given post graphQL request with body graphQL/getCodeValidation.graphql override table values
      | enrollmentId | SCENARIO_DATA:enrollmentId |
      | phoneCode    | SCENARIO_DATA:phoneCode    |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.codeValidation.status       | CONTAINS:CODE_APPROVED |
      | data.codeValidation.enrollmentId | NOT NULL               |
    Given I set the new Password with wrong police requirements with body graphQL/getEnrollUser.graphql
      | enrollmentId | SCENARIO_DATA:enrollmentId    |
      | username     | Automation2023                |
      | password     | SCENARIO_DATA:newPassTooShort |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: restore password policy configuration (4)
    Given restore password policy configuration

  @Email
  Scenario: BCPRXRP-918 Create User into Cognito
    Given Delete user Automation2023 from Cognito
    Given Set isUserCreation as true
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
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId   |
      | String | :status_      | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
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
  Scenario: BCPRXRP-327-881-Update password into Enrolment
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

  Scenario: BCPRXRP-920 - Try update password with wrong policy
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023              |
      | PASSWORD | SCENARIO_DATA:newPassUpdate |
    Given I set the new Password with wrong police requirements with body graphQL/getPasswordUpdate.graphql
      | currentPassword | SCENARIO_DATA:newPassUpdate |
      | newPassword     | SCENARIO_DATA:newPass       |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |


  Scenario: BCPRXRP-921 - Try update password including the username
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023           |
      | PASSWORD | SCENARIO_DATA:passBackUp |
    Given I set the new Password with wrong police requirements with body graphQL/getPasswordUpdateWithUsername.graphql
      | currentPassword     | SCENARIO_DATA:passBackUp          |
      | newPassWithUsername | SCENARIO_DATA:newPassWithUsername |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password must not contain the username |


  Scenario: BCPRXRP-922 - Try update password including a blackListed character
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023           |
      | PASSWORD | SCENARIO_DATA:passBackUp |
    Given I set the new Password with wrong police requirements with body graphQL/getPasswordUpdateWithBlackListed.graphql
      | currentPassword        | SCENARIO_DATA:passBackUp             |
      | newPassWithBlackListed | SCENARIO_DATA:newPassWithBlackListed |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |


  Scenario: BCPRXRP-1055 Try updating the same current password
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023           |
      | PASSWORD | SCENARIO_DATA:passBackUp |
    Given I set the new Password with wrong police requirements with body graphQL/getPasswordSetCurrent.graphql
      | currentPassword | SCENARIO_DATA:passBackUp |
      | newPassword     | SCENARIO_DATA:passBackUp |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password already exist, a different password need to be entered |


  Scenario: BCPRXRP-1057 - Try updating password but it is too short
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Automation2023           |
      | PASSWORD | SCENARIO_DATA:passBackUp |
    Given I set the new Password with wrong police requirements with body graphQL/getPasswordSetTooShort.graphql
      | currentPassword | SCENARIO_DATA:passBackUp      |
      | newPassword     | SCENARIO_DATA:newPassTooShort |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: restore password policy configuration(4)
    Given restore password policy configuration