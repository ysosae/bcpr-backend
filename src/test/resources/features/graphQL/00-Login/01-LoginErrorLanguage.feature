@Graphql @Login @IGNORE
Feature: Login

  Scenario: BCPRXRP-500-User perform graphql log-in Error EN
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala |
      | PASSWORD | Test165263 |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |

  Scenario: BCPRXRP-501-User perform graphql log-in Error ES
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala |
      | PASSWORD | Test165263 |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:No pudieron validarse los datos ingresados/¡Acceso denegado./Ha ocurrido un error inesperado |

  Scenario: BCPRXRP-502-User perform graphql log-in Error Another Language RU
    Given Override language to ru
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala |
      | PASSWORD | Test165263 |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:No pudieron validarse los datos ingresados/¡Acceso denegado./Ha ocurrido un error inesperado |

    Scenario: BCPRXRP-562-Verify policy password - (Contains username)
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala         |
      | PASSWORD | Lucycatala165263** |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |

      Scenario: BCPRXRP-562-Verify policy password - (Not contains lower-case)
        Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala     |
      | PASSWORD | LUCY14165263** |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |

     Scenario: BCPRXRP-562-Verify policy password- (Not contains Upper-case)
     Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala     |
      | PASSWORD | este14165263** |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |

     Scenario: BCPRXRP-562-Verify policy password- (Not contains letters)
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala     |
      | PASSWORD | 9456114165263** |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |

     Scenario: BCPRXRP-562-Verify policy password- (Not contains characters specials)
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | lucycatala     |
      | PASSWORD | Autom114165263 |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied!/An unexpected error |
