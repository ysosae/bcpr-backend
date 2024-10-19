@Graphql @secondChance @crt
Feature: Claims - List - graphQL

  Background:
    Given Set Facundo as main test user

  Scenario: BCPRXRP-505-List Claim Types - ES
    Given Override language to es
    Given post a graphQL request using graphQL/listClaimTypes.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
#      | LIST:data.listClaimTypes.id                  | INTEGER                                                                                                                                                                                                                                       |
      | LIST:data.listClaimTypes.title               | OR:Cargo no autorizado/Transacción duplicada/Monto incorrecto/ATM no expidió el dinero/ATM no expidió el total del dinero/Crédito no procesado/Bienes o servicios no proporcionados/Transacción cancelada/Bienes o servicios defectuosos/Otro |
#      | LIST:data.listClaimTypes.description         | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.isCardLockedByFraud | BOOLEAN                                                                                                                                                                                                                                       |
#      | LIST:data.listClaimTypes.details             | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.title       | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.id          | NOT NULL                                                                                                                                                                                                                                      |

  Scenario: BCPRXRP-506-List Claim Types - EN
    Given Override language to en
    Given post a graphQL request using graphQL/listClaimTypes.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
 #     | LIST:data.listClaimTypes.id                  | INTEGER                                                                                                                                                                                                                               |
 #     | LIST:data.listClaimTypes.title               | OR:Unauthorized charge/Duplicate transaction/Wrong amount/ATM did not issue the money/ATM did not issue the total money/Credit not processed/Assets or services not provided./Transaction canceled/Defective assets or services/Other |
      | LIST:data.listClaimTypes.description         | NOT NULL                                                                                                                                                                                                                              |
 #     | LIST:data.listClaimTypes.isCardLockedByFraud | BOOLEAN                                                                                                                                                                                                                               |
#      | LIST:data.listClaimTypes.details             | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.title       | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.id          | NOT NULL                                                                                                                                                                                                                                      |


  Scenario: BCPRXRP-507-List Claim Types - RU
    Given Override language to ru
    Given post a graphQL request using graphQL/listClaimTypes.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
#      | LIST:data.listClaimTypes.id                  | INTEGER                                                                                                                                                                                                                                       |
      | LIST:data.listClaimTypes.title               | OR:Cargo no autorizado/Transacción duplicada/Monto incorrecto/ATM no expidió el dinero/ATM no expidió el total del dinero/Crédito no procesado/Bienes o servicios no proporcionados/Transacción cancelada/Bienes o servicios defectuosos/Otro |
#      | LIST:data.listClaimTypes.description         | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.isCardLockedByFraud | BOOLEAN                                                                                                                                                                                                                                       |
#      | LIST:data.listClaimTypes.details             | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.title       | NOT NULL                                                                                                                                                                                                                                      |
#      | LIST:data.listClaimTypes.details.id          | NOT NULL                                                                                                                                                                                                                                      |

