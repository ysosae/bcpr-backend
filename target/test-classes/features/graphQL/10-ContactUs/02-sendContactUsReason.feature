@Graphql @secondChance @Email @notification @crt
Feature: GraphQL- ContactUs

   Scenario: BCPRXRP-813-get Mutation Send Contact Us Request Not logged - ES
    Given Override language to es
    Given post a graphQL request using graphQL/getMutationSendContactUsRequest.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.sendContactUsRequest.message | Message processed successfully |

  Scenario: BCPRXRP-814-get Mutation Send Contact Us Request Not logged - EN
    Given Override language to en
    Given post a graphQL request using graphQL/getMutationSendContactUsRequest.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.sendContactUsRequest.message | Message processed successfully |

  Scenario: BCPRXRP-815-get Mutation Logged Send Contact Us Request - EN
    Given Override language to en
    Given post a graphQL request using graphQL/getMutationLoggedSendContactUsRequest.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.sendLoggedContactUsRequest.message | Message processed successfully |

  Scenario: BCPRXRP-816-get Mutation Logged Send Contact Us Request - ES
    Given Override language to es
    Given post a graphQL request using graphQL/getMutationLoggedSendContactUsRequest.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.sendLoggedContactUsRequest.message | Message processed successfully |

  Scenario: BCPRXRP-898-get Mutation Not Logged Send Contact Us Request - ERROR (Empty phoneNumbers)
    Given Override language to en
    Given post a graphQL request using graphQL/getMutationSendContactUsRequestERROR.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided could not be validated |