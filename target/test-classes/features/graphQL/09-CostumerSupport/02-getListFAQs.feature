@Graphql @secondChance @crt
Feature: Costumer Support - FAQs

  Scenario: BCPRXRP-513-Get list FAQs English
    Given Override language to en
    Given post a graphQL request using graphQL/getListFAQsByLanguage.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listFAQS.questionId           | REGEX:^[0-9]{1,20} |
      | LIST:data.listFAQS.question             | NOT NULL           |
      | LIST:data.listFAQS.answer               | NOT NULL           |
      | LIST:data.listFAQS.category.categoryId  | NOT NULL           |
      | LIST:data.listFAQS.category.description | NOT NULL           |

  Scenario: BCPRXRP-512-Get list FAQs Spanish
    Given Override language to es
    Given post a graphQL request using graphQL/getListFAQsByLanguage.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listFAQS.questionId           | REGEX:^[0-9]{1,20}                                                                                                                                                                                                                                                                                                                                                                                             |
      | LIST:data.listFAQS.question             | OR:¿Qué significa activar/¿Qué información necesito/¿Qué otro mecanismo/Olvidé el ID de usuario/¿Con esta aplicación también/¿Qué significa bloquear/¿Por qué debería bloquear/¿El bloqueo temporero/Si tengo más de una/¿Puede un bloqueo/¿Cuánto tiempo/¿Si mi tarjeta/¿Qué es disputar/¿Cómo puedo pagar/¿Cuánto tiempo/Qué pasa si pago/¿Cuáles son los/¿Hay algún/¿Qué es el programa/¿Cómo puedo redimir |
      | LIST:data.listFAQS.answer               | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |
      | LIST:data.listFAQS.category.categoryId  | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |
      | LIST:data.listFAQS.category.description | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |

  Scenario: BCPRXRP-514-Get list FAQs Another Language RU
    Given Override language to ru
    Given post a graphQL request using graphQL/getListFAQsByLanguage.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | LIST:data.listFAQS.questionId           | REGEX:^[0-9]{1,20}                                                                                                                                                                                                                                                                                                                                                                                             |
      | LIST:data.listFAQS.question             | OR:¿Qué significa activar/¿Qué información necesito/¿Qué otro mecanismo/Olvidé el ID de usuario/¿Con esta aplicación también/¿Qué significa bloquear/¿Por qué debería bloquear/¿El bloqueo temporero/Si tengo más de una/¿Puede un bloqueo/¿Cuánto tiempo/¿Si mi tarjeta/¿Qué es disputar/¿Cómo puedo pagar/¿Cuánto tiempo/Qué pasa si pago/¿Cuáles son los/¿Hay algún/¿Qué es el programa/¿Cómo puedo redimir |
      | LIST:data.listFAQS.answer               | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |
      | LIST:data.listFAQS.category.categoryId  | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |
      | LIST:data.listFAQS.category.description | NOT NULL                                                                                                                                                                                                                                                                                                                                                                                                       |

  Scenario: BCPRXRP-916-Adjust text "What is the Recoompensa Program?" FAQs English
    Given Override language to en
    Given post a graphQL request using graphQL/getListFAQsByLanguage.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listFAQS.questionId[18]           | 19                                                                                                                     |
      | data.listFAQS.question[18]             | CONTAINS:What is the Recoompensa Program?                                                                              |
      | data.listFAQS.answer[18]               | CONTAINS:Accumulation of points varies by credit union. For details on ReCoompensa program, contact your credit union. |
      | data.listFAQS.category[18].categoryId  | 4                                                                                                                      |
      | data.listFAQS.category[18].description | Rewards                                                                                                                |

  Scenario: BCPRXRP-915-Adjust text "What is the Recoompensa Program?" FAQs Spanish
    Given Override language to es
    Given post a graphQL request using graphQL/getListFAQsByLanguage.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listFAQS.questionId[18]           | 19                                                                                                                                        |
      | data.listFAQS.question[18]             | CONTAINS:¿Qué es el programa de ReCoompensa?                                                                                              |
      | data.listFAQS.answer[18]               | CONTAINS:La acumulación de puntos varía por Cooperativa. Para obtener detalles del programa de ReCoompensa, comuníquese a su cooperativa. |
      | data.listFAQS.category[18].categoryId  | 4                                                                                                                                         |
      | data.listFAQS.category[18].description | Recompensas                                                                                                                               |
