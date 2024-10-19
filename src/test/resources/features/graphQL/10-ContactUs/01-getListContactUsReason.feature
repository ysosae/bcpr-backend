@Graphql @secondChance @crt
Feature: GraphQL- ContactUs

  Scenario: BCPRXRP-515-get List Contact Us English
    Given Override language to en
    Given post a graphQL request using graphQL/getListContactUsReason.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listContactUsReason.id          | REGEX:^[0-9]{1,3} |
      | LIST:data.listContactUsReason.description | NOT NULL          |

  Scenario: BCPRXRP-516-get List Contact Us Spanish
    Given Override language to es
    Given post a graphQL request using graphQL/getListContactUsReason.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listContactUsReason.id          | REGEX:^[0-9]{1,3} |
      | LIST:data.listContactUsReason.description | NOT NULL |

  Scenario: BCPRXRP-517-get List Contact Us Another Language like RU
    Given Override language to ru
    Given post a graphQL request using graphQL/getListContactUsReason.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listContactUsReason.id          | REGEX:^[0-9]{1,3}                                                                                |
      | LIST:data.listContactUsReason.description | OR:Abrir un nuevo producto financiero/Mi tarjeta de credito está bloqueada/Enviar felicitaciones |

