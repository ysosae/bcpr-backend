@Graphql @secondChance @Notification @notification @crt
Feature: GraphQL-Notifications-SMS

  @Email
  Scenario: BCPRXRP-847-Send SMS Notification By Mutation
    Given post a graphQL request using graphQL/getSendSMSNotificationByMutation.graphql
    And I print out the results of the response
    When The response is 200
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendSMSNotification.response.id | NOT NULL |
      | data.sendSMSNotification.status.msg  | OK       |

  Scenario: BCPRXRP-824-Send SMS Notification By Mutation (ERROR)- EN
    Given post a graphQL request using graphQL/getSendSMSNotificationEmpty.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | errors[0].message | FLAG:The data provided could not be validated/Los datos ingresados no pudieron ser validados. |

  Scenario: Send SMS Notification By Mutation (ERROR)- ES
    Given Override language to es
    Given post a graphQL request using graphQL/getSendSMSNotificationEmpty.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | errors[0].message | OR:Los datos ingresados no pudieron ser validados. |

  Scenario: BCPRXRP-848-Send SMS Notification By Mutation (ERROR DNSY)
    Given post a graphQL request using graphQL/getSendSMSNotificationErrorDNSY.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendSMSNotification.status.code | OR:404/400/ETIMEOUT/200                                             |
      | data.sendSMSNotification.status.msg  | OR:The search element was not found/Bad Request/Connect ETIMEOUT/OK |


  Scenario: BCPRXRP-861-Send SMS Notification By Mutation (ERROR Microservices Notification)
    Given post a graphQL request using graphQL/getSendSMSNotificationErrorDNSY.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendSMSNotification.status.code | OR:404/400/ETIMEOUT/200                                             |
      | data.sendSMSNotification.status.msg  | OR:The search element was not found/Bad Request/Connect ETIMEOUT/OK |

  Scenario: BCPRXRP-904-Send SMS Notification By Mutation (ERROR EMPTY REQUIRED FIELD DNSY)
    Given post a graphQL request using graphQL/getSendSMSNotificationErrorEmptyRequiredFieldDNSY.graphql
    And I print out the results of the response
    When The response is 400
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | errors[0].message | OR:Your request cannot be process properly |

  @IGNORE
  @Alarm
  Scenario: BCPRXRP-1081-The Alarm SMS Notification was Throw into Container services
   Then validate alarm of queue sms notification has status value ALARM into Application services

  @IGNORE
  @Alarm
  Scenario: BCPRXRP-1253-The Alarm SMS Notification was Throw into MSS services
    Then validate alarm of queue sms notification has status value ALARM into MSS services