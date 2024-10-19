@Graphql @IGNORE
Feature: GraphQL-Settings

  Scenario:BCPRXRP-325-BCPRXRP-561-Update password into Settings
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Facundo2023            |
      | PASSWORD | SCENARIO_DATA:password |
    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
      | currentPassword | SCENARIO_DATA:password |
      | newPassword     | newPassword            |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Facundo2023                 |
      | PASSWORD | SCENARIO_DATA:newPassUpdate |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user username
    And delete item dynamoDb by table name LastPassword and item name username

#    scenario: BCPRXRP-665-Pasword Update more than one into Settings
#    Given Set Automation as main test user
#    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
#      | currentPassword | Test**2303291338 |
#      | newPassword     | Test**2303291339 |
#    And I print out the results of the response
#    When The response is 200
#    And I compare response <Path> show the <Values>
#      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
#    Given post graphQL request with body graphQL/login.graphql override table values
#      | USERNAME | Automation2023   |
#      | PASSWORD | Test**2303291339 |
##    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
##      | currentPassword | Test**2303291339 |
##      | newPassword     | Test**2303291340 |
##    And I print out the results of the response
##    When The response is 200
##    And I compare response <Path> show the <Values>
##      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
##    Given post graphQL request with body graphQL/login.graphql override table values
##      | USERNAME | Automation2023   |
##      | PASSWORD | Test**2303291340 |
##    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
##      | currentPassword | Test**2303291340 |
##      | newPassword     | Test**2303291341 |
##    And I print out the results of the response
##    When The response is 200
##    And I compare response <Path> show the <Values>
##      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
##    Given post graphQL request with body graphQL/login.graphql override table values
##      | USERNAME | Automation2023   |
##      | PASSWORD | Test**2303291341 |

  Scenario:BCPRXRP-797-Validate a field in the database to store value of matching validation with passwords prior to 10 records
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Facundo2023            |
      | PASSWORD | SCENARIO_DATA:password |
    Given post graphQL request with body graphQL/getPasswordUpdate.graphql override table values
      | currentPassword | SCENARIO_DATA:password |
      | newPassword     | newPassword            |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.passwordUpdate.status | CONTAINS:PASSWORD_UPDATED |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Facundo2023                 |
      | PASSWORD | SCENARIO_DATA:newPassUpdate |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user username
    And delete item dynamoDb by table name LastPassword and item name username