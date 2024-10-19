@Graphql @secondChance @crt
Feature: GraphQL-Settings

  Scenario: BCPRXRP-279 get Profile Data English
    Given Override language to en
    And post a graphQL request using graphQL/getProfile_ES.graphql
    When I print out the results of the response
    And The response is 200
    Then I compare response <Path> show the <Values>
      | data.getProfile.userName                                                | NOT NULL                                                                                                                                   |
      | data.getProfile.name                                                    | NOT NULL                                                                                                                                   |
      | data.getProfile.lastname                                                | NOT NULL                                                                                                                                   |
      | data.getProfile.phoneNumber                                             | NOT NULL                                                                                                                                   |
      | data.getProfile.email                                                   | EMAIL                                                                                                                                      |
      | data.getProfile.callCenterPhoneNumbers[0].institutionId                 | NUMBER                                                                                                                                     |
      | data.getProfile.callCenterPhoneNumbers[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                       |
      | data.getProfile.callCenterPhoneNumbers[0].icon                          | NOT NULL                                                                                                                                   |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].description | CONTAINS:Member Service Center                                                                                                             |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                      |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].schedule    | CONTAINS:Member service available 24/7 all year, only for Transactions related to your credit card VISA or Mastercard of the Credit Union. |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].email       | EMAIL                                                                                                                                      |

  Scenario: BCPRXRP-280 get Profile Data Spanish
    Given Override language to es
    And post a graphQL request using graphQL/getProfile_EN.graphql
    When I print out the results of the response
    And The response is 200
    Then I compare response <Path> show the <Values>
      | data.getProfile.userName                                                | NOT NULL                                                                                                                                                    |
      | data.getProfile.name                                                    | NOT NULL                                                                                                                                                    |
      | data.getProfile.lastname                                                | NOT NULL                                                                                                                                                    |
      | data.getProfile.phoneNumber                                             | NOT NULL                                                                                                                                                    |
      | data.getProfile.email                                                   | EMAIL                                                                                                                                                       |
      | data.getProfile.callCenterPhoneNumbers[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.getProfile.callCenterPhoneNumbers[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.getProfile.callCenterPhoneNumbers[0].icon                          | NOT NULL                                                                                                                                                    |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].email       | EMAIL                                                                                                                                                       |

  Scenario: BCPRXRP-508 get Profile Data Another Language return Default
    Given Override language to ru
    And post a graphQL request using graphQL/getProfile_EN.graphql
    When I print out the results of the response
    And The response is 200
    Then I compare response <Path> show the <Values>
      | data.getProfile.userName                                                | NOT NULL                                                                                                                                                    |
      | data.getProfile.name                                                    | NOT NULL                                                                                                                                                    |
      | data.getProfile.lastname                                                | NOT NULL                                                                                                                                                    |
      | data.getProfile.phoneNumber                                             | NOT NULL                                                                                                                                                    |
      | data.getProfile.email                                                   | EMAIL                                                                                                                                                       |
      | data.getProfile.callCenterPhoneNumbers[0].institutionId                 | NUMBER                                                                                                                                                      |
      | data.getProfile.callCenterPhoneNumbers[0].institutionName               | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.getProfile.callCenterPhoneNumbers[0].icon                          | NOT NULL                                                                                                                                                    |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].description | CONTAINS:Centro de Servicio al Socio                                                                                                                        |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].phoneNumber | CONTAINS:787-641-2310                                                                                                                                       |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].schedule    | CONTAINS:Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa. |
      | data.getProfile.callCenterPhoneNumbers.callCenterOptions[0].email       | EMAIL                                                                                                                                                       |
