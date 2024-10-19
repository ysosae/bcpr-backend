@Graphql @secondChance @PasswordPolicy
Feature: Regain Access.

  Scenario: BCPRXRP-287-Get RegainAccess Validation
    #Given Set yohara as main test user
    #Given Searching with the email pruebabcpr4@gmail.com filter is a user exists in cognito
    Given Set isUserCreation as true
#    Given post a graphQL request using graphQL/getRegainAccessValidationLucy.graphql
    Given post a graphQL request using graphQL/getRegainAccessValidationEdgardo.graphql
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
      | String | :regainAccessId | regainAccessId |
      | String | :status_        | CODE_RESENT    |
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


  Scenario: BCPRXRP-290-Set NewPassword into Recover Password
    Given post graphQL request with body graphQL/getSetNewPassword.graphql override table values
      | password | newPass |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |
    Given Set isUserCreation as false
#    Given post graphQL request with body graphQL/login.graphql override table values
#      | USERNAME | Automation2023        |
#      | PASSWORD | SCENARIO_DATA:newPass |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Edgardo2023        |
      | PASSWORD | SCENARIO_DATA:newPass |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    And Wait 5 seconds between request
    #Then validate LastPassword entry in DynamoDB for user Automation2023
    Then validate LastPassword entry in DynamoDB for user Edgardo2023

#  @notification
#  Scenario: BCPRXRP-771 Delete user into cognito
#    Given Delete user Automation2023 from Cognito
#    Given Set isUserCreation as false
#    And clean token of Automation2023 user
#    And delete item dynamoDb by table name LastPassword and item name Automation2023



