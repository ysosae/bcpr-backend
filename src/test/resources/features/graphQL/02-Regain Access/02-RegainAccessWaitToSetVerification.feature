@Graphql @IGNORE
Feature: Regain Access.

  @IGNORE
  Scenario: BCPRXRP-673-Get Regain Access wait 360 seconds to set verification code
    Given Set Automation as main test user
    Given Searching with the email pruebabcpr2@gmail.com filter is a user exists in cognito
    Given post a graphQL request using graphQL/getRegainAccessValidation.graphql
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
    Then I wait during 360 seconds before send the code
    Given post a graphQL request using graphQL/getRegainAccessCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | CONTAINS:The provided code is not valid |







