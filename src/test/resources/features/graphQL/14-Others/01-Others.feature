@Graphql @IGNORE
Feature: GraphQL-Settings

  Scenario: BCPRXRP-744-Minimum allowed version
    Given post a graphQL request using graphQL/minimumMobileVersionAllow.graphql
    And I print out the results of the response
    And The response is 400
    Then I compare response <Path> show the <Values>
      | errors[0].message | Your request cannot be process properly |
