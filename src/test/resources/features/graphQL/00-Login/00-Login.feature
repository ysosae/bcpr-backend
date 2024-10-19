@Graphql @Login @secondChance @crt
Feature: Login

  Scenario: BCPRXRP-278-BCPRXRP-345-User perform graphql log-in
    Given Set yalithza as main test user
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |

  Scenario: BCPRXRP-495 User perform graphql log-out
    Given Set yalithza as main test user
    Given perform login using data in graphQL/login.graphql
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    Then post a graphQL request using graphQL/logout.graphql
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.logout.message | Logout successfull |

  Scenario: BCPRXRP-853 [EN] - User perform graphql log-in with generic message with wrong password
    Given Override language to en
#    Given post graphQL request with body graphQL/login.graphql override table values
#      | USERNAME | Yalithza123 |
#      | PASSWORD | Test165263  |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Yalithza123                 |
      | PASSWORD | SCENARIO_DATA:wrongPassword |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message    | OR:The data provided is incorrect/Access denied!/An unexpected error                                            |
      | errors[0].extensions | OR:Username or Password not valid/If the error persists contact your Credit Union/Your access has been blocked. |


  Scenario: BCPRXRP-392 [ES] - User perform graphql log-in with generic message with wrong password
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | Yalithza123                 |
      | PASSWORD | SCENARIO_DATA:wrongPassword |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message    | OR:No pudieron validarse los datos ingresados/¡Acceso denegado.                                           |
      | errors[0].extensions | OR:password incorrectos/Si el error persiste comuníquese con su cooperativa/Su acceso ha sido bloqueando. |


  Scenario: BCPRXRP-393-User perform graphql log-in with generic message with password correct and username wrong
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:wrongUsername |
      | PASSWORD | Test12345                   |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied/An unexpected error |

  Scenario:BCPRXRP-2-User perform graphql log-in with email address and password
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:validEmail |
      | PASSWORD | Test12345                |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied/An unexpected error |

  Scenario: BCPRXRP-394-User perform graphql log-in with generic when not exist user into Cognito
    Given Override language to en
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:wrongUsername |
      | PASSWORD | Test123452                  |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | OR:The data provided is incorrect/Access denied/An unexpected error |

  Scenario: BCPRXRP-854-BCPRXRP-390-User perform graphql log-in with generic and refresh token into Cognito
    Given Override language to en
    Given save user with Role Admins as main test user
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then save key refreshToken and path data.login.refreshToken as context variable
    And save cognito attribute sub by username as context variable
    Given post graphQL request with body graphQL/refreshToken.graphql override table values
      | token   | SCENARIO_DATA:refreshToken |
      | usersub | SCENARIO_DATA:sub          |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.refreshToken.idToken | NOT NULL |