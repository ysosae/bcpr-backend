@Graphql @secondChance @crt
Feature: List Faqs For Rewards

  Scenario: BCPRXRP-294-Get List Spanish Faqs For Rewards from logged user
    Given Override language to es
    Given post a graphQL request using graphQL/getListFaqsForRewardsSpanish.graphql
    Then I print out the results of the response
    Then The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/getListFaqsForRewardsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listFaqsForRewards.questionId           | NOT NULL                                                                                                                                  |
      | LIST:data.listFaqsForRewards.question             | OR:¿Qué es el programa de ReCoompensa?/¿Cómo puedo redimir mis puntos ReCoompensa?                                                        |
      | data.listFaqsForRewards[0].answer                 | CONTAINS:La acumulación de puntos varía por Cooperativa. Para obtener detalles del programa de ReCoompensa, comuníquese a su cooperativa. |
      | LIST:data.listFaqsForRewards.category.categoryId  | NOT NULL                                                                                                                                  |
      | LIST:data.listFaqsForRewards.category.description | CONTAINS:Recompensas                                                                                                                      |

  Scenario: BCPRXRP-295-Get List English Faqs For Rewards from logged user
    Given Override language to en
    Given post a graphQL request using graphQL/getListFaqsForRewardsEnglish.graphql
    Then I print out the results of the response
    Then The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/getListFaqsForRewardsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listFaqsForRewards.questionId           | NOT NULL                                                                                                              |
      | LIST:data.listFaqsForRewards.question             | OR:What is the ReCoompensa program?/How can I redeem my ReCoompensa points?                                           |
      | data.listFaqsForRewards[0].answer                 | CONTAINS:Accumulation of points varies by credit union. For details on ReCoompensa program, contact your credit union |
      | LIST:data.listFaqsForRewards.category.categoryId  | NOT NULL                                                                                                              |
      | LIST:data.listFaqsForRewards.category.description | CONTAINS:Rewards                                                                                                      |


  Scenario: BCPRXRP-296-Get List English Faqs For Rewards from logged user (BadRequest)
    Given Override language to ru
    Given post a graphQL request using graphQL/getListFaqsForRewardsEnglish.graphql
    Then I print out the results of the response
    Then The response is 200
    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/getListFaqsForRewardsOutput.json
    Then I compare response <Path> show the <Values>
      | LIST:data.listFaqsForRewards.questionId           | NOT NULL                                                                                                                                  |
      | LIST:data.listFaqsForRewards.question             | OR:¿Qué es el programa de ReCoompensa?/¿Cómo puedo redimir mis puntos ReCoompensa?                                                        |
      | data.listFaqsForRewards[0].answer                 | CONTAINS:La acumulación de puntos varía por Cooperativa. Para obtener detalles del programa de ReCoompensa, comuníquese a su cooperativa. |
      | LIST:data.listFaqsForRewards.category.categoryId  | NOT NULL                                                                                                                                  |
      | LIST:data.listFaqsForRewards.category.description | CONTAINS:Recompensas                                                                                                                      |
