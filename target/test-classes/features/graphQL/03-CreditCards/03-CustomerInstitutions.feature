@Graphql @secondChance @crt
Feature: Customer Institutions

  Scenario: BCPRXRP-293-Get customer institution list from logged user
    Given Set yuliet as main test user
    Given post a graphQL request using graphQL/listCustomerInstitutions.graphql
    Then I print out the results of the response
    Then The response is 200
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/getListCustomerInstitutionsOutput.json
    Then I compare response <Path> show the <Values>
      | data.listCustomerInstitutions[0].id    | NOT NULL |
      | data.listCustomerInstitutions[0].name  | NOT NULL |
      | data.listCustomerInstitutions[0].email | EMAIL    |