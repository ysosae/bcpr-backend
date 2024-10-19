@Graphql @secondChance @crt
Feature: Costumer Support - Call Center

  Scenario: BCPRXRP-510-Get Call Center English - Not logged
    Given Override language to en
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                     |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Member Service Center                                                                                                             |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                      |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Member service available 24/7 all year, only for Transactions related to your credit card VISA or Mastercard of the Credit Union. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-511-Get Call Center Spanish - Not logged
    Given Override language to es
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-509-Get Call Center Another Language RU - Not logged
    Given Override language to ru
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-1143-Get Call Center English - Logged
    Given Set yalithza as main test user
    Given Override language to en
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                     |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Member Service Center                                                                                                             |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                      |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Member service available 24/7 all year, only for Transactions related to your credit card VISA or Mastercard of the Credit Union. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-1144-Get Call Center Spanish - Logged
    Given Set yalithza as main test user
    Given Override language to es
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-1145-Get Call Center Another Language RU - Logged
    Given Set yalithza as main test user
    Given Override language to ru
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-1235-Get IVR - Button English - Not logged
    Given Override language to en
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                     |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                       |
      | data.listCallCenters[0].icon                          | OR:support_agent/credit_card                                                                                                               |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Member Service Center                                                                                                             |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                      |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Member service available 24/7 all year, only for Transactions related to your credit card VISA or Mastercard of the Credit Union. |
    And validate multiple IVR call center

  Scenario: BCPRXRP-1234-Get IVR - Button Spanish - Not logged
    Given Override language to es
    Given post a graphQL request using graphQL/getCallCenterIVRButton.graphql
    When I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.listCallCenters[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.listCallCenters[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters[0].icon                          | OR:support_agent/credit_card                                                                                                                                |
      | data.listCallCenters.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.listCallCenters.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.listCallCenters.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
    And validate multiple IVR call center