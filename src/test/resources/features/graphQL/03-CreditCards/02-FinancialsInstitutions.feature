@Graphql @secondChance @crt
Feature: Financials institution

  Scenario: BCPRXRP-292-Get Financials institution list from logged user
    Given post a graphQL request using graphQL/listInstitutions.graphql
    Then I print out the results of the response
    Then The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/listInstitutionsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listInstitutions.id           | NOT NULL                                |
      | LIST:data.listInstitutions.name         | NOT NULL                                |
      | LIST:data.listInstitutions.email        | EMAIL                                   |
