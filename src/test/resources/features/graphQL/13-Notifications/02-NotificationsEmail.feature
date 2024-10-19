@Graphql @secondChance @Notification @notification @crt
Feature: GraphQL-Notifications-Email

  @Email
  Scenario: BCPRXRP-851-Send EMAIL Notification By Mutation (SUCCESS)
    Given post a graphQL request using graphQL/getSendEmailNotification.graphql
    And I print out the results of the response
    When The response is 200
#    Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendEmailNotification.status.code | 200 |
      | data.sendEmailNotification.status.msg  | OK  |

  Scenario: BCPRXRP-850-Send EMAIL Notification By Mutation (ERROR)
    Given post a graphQL request using graphQL/getSendEmailNotificationEmpty.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendEmailNotification.status.code | OR:404/400/ETIMEOUT/200                                             |
      | data.sendEmailNotification.status.msg  | OR:The search element was not found/Bad Request/Connect ETIMEOUT/OK |

  Scenario: BCPRXRP-849-Send EMAIL Notification By Mutation (ERROR DNSY)
    Given post a graphQL request using graphQL/getSendEmailNotificationErrorDNSY.graphql
    And I print out the results of the response
    When The response is 200
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | data.sendEmailNotification.status.code | OR:404/400/ETIMEOUT/200                                             |
      | data.sendEmailNotification.status.msg  | OR:The search element was not found/Bad Request/Connect ETIMEOUT/OK |

  Scenario: BCPRXRP-860-Send EMAIL Notification By Mutation (ERROR Microservices Notification)
    Given post a graphQL request using graphQL/getSendEmailNotificationErrorDNSY.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.sendEmailNotification.status.code | OR:404/400/ETIMEOUT/200                                             |
      | data.sendEmailNotification.status.msg  | OR:The search element was not found/Bad Request/Connect ETIMEOUT/OK |

  Scenario: BCPRXRP-945-Send EMAIL Notification By Mutation (ERROR EMPTY REQUIRED FIELD DNSY)
    Given post a graphQL request using graphQL/getSendEmailNotificationErrorEmptyRequiredFieldDNSY.graphql
    And I print out the results of the response
    When The response is 400
    #Then I validate API response with Schema statement referenced at ./data/schemas/graphQL/sendSMSNotificationByMutationOutput.json
    And I compare response <Path> show the <Values>
      | errors[0].message | OR:Your request cannot be process properly |

  @IGNORE
  @Alarm
  Scenario: BCPRXRP-1082-The Alarm EMAIL Notification was Throw into Container services
    Then validate alarm of queue Email notification has status value ALARM into Application services


  @IGNORE
  @Alarm
  Scenario: BCPRXRP-1254-The Alarm EMAIL Notification was Throw into MSS services
    Then validate alarm of queue Email notification has status value ALARM into MSS services