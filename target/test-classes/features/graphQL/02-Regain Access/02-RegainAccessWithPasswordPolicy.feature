@Graphql @PasswordPolicy
Feature: Regain Access - Password Police

  Scenario: BCPRXRP-287-Get RegainAccess Validation
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

  Scenario: BCPRXRP-289-Get CodeValidation into Regain Access
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


  Scenario: BCPRXRP-290-925-Set NewPassword with wrong police requirements into Recover Password
    Given I set the new Password with wrong police requirements with body graphQL/getSetNewPassword.graphql
      | password | SCENARIO_DATA:newPassUpdate |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: BCPRXRP-1052 Try setting the same current password
    Given I set the new Password with wrong police requirements with body graphQL/setSameCurrentPassword.graphql
      | password | SCENARIO_DATA:passBackUp |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password already exist, a different password need to be entered |

  Scenario: BCPRXRP-926 - Try set a password including the username
    Given I set the new Password with wrong police requirements with body graphQL/setNewPassword.graphql
      | password   | SCENARIO_DATA:newPassWithUsername   |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password must not contain the username |

  Scenario: BCPRXRP-927 - Try update password including a blackListed character
    Given I set the new Password with wrong police requirements with body graphQL/setNewPasswordWithBlackListed.graphql
      | password    | SCENARIO_DATA:newPassWithBlackListed   |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

  Scenario: BCPRXRP-1053 - Try updating password but it is too short
    Given I set the new Password with wrong police requirements with body graphQL/setNewPasswordTooShort.graphql
      | password    | SCENARIO_DATA:newPassTooShort   |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | Password does not meet security |

#  Scenario: BCPRXRP-771 Delete user into cognito
#    Given Delete user Automation2023 from Cognito
#    And clean token of Automation2023 user
#    And delete item dynamoDb by table name LastPassword and item name Automation2023
#    Given Set isUserCreation as false


