@Graphql @crt @IGNORE
Feature: health Check

  Scenario: BCPRXRP-958 - launch health check status
    Given post a graphQL request using graphQL/healthCheck.graphql
    And I print out the results of the response
    When The response is 400
    Then I compare response <Path> show the <Values>
      | errors[0].message | Your request cannot be process properly |


  Scenario: BCPRXRP-993 - get minimum mobile version
    Given post a graphQL request using graphQL/minimumMobileVersionAllow.graphql
    And I print out the results of the response
    When The response is 400
    Then I compare response <Path> show the <Values>
      | errors[0].message | Your request cannot be process properly |
