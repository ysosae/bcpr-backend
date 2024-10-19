@Graphql @Login
Feature: BCPRI-319 User log-in with inactive timeout session

  Scenario: BCPRXRP-870 - User log-in with inactive timeout session
    Given Set yalithza as main test user
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login                                | NOT NULL |
      | data.login.validUserSessionTimeInSeconds  | 420 |
      | data.login.maxAllowedInactivityInSeconds  | 30  |


  @IGNORE
  Scenario: BCPRXRP-871 User log-in and wait timeout session
    Given Set yalithza as main test user
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login                                | NOT NULL |
      | data.login.validUserSessionTimeInSeconds  | 420 |
      | data.login.maxAllowedInactivityInSeconds  | 30  |
    And Wait 3600 seconds between request
    And post a graphQL request using graphQL/getProfile_EN.graphql
    When I print out the results of the response
    And The response is 200