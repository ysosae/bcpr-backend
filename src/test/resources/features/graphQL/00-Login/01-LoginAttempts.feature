@Graphql @Login @attempts @notification
Feature: Login Attempts

  @Email
  Scenario: BCPRXRP-346-BCPRXRP-887-Block user after 3th try failed login [EN]-BCPRI-221
    Given Set edgardo as main test user
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test000000             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | OR:The data provided is incorrect/Access denied!/An unexpected error              |
      | errors[0].extensions.description | OR:If the error persists contact your Credit Union/Username or Password not valid |
    And Short wait between request
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test111111             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | OR:The data provided is incorrect/Access denied!/An unexpected error              |
      | errors[0].extensions.description | OR:If the error persists contact your Credit Union/Username or Password not valid |
    And Short wait between request
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | OR:The data provided is incorrect/Access denied!/An unexpected error |
      | errors[0].extensions.description | CONTAINS:The max attempts to login was reached                       |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: LoginAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value login into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: facundovillalba-uat expected Enable: false
    And Short wait between request
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Your access has been blocked. Recover access by pressing |
    And delete item dynamoDb by table name LoginAttempts and item name username
    And delete item dynamoDb by table name BlockReason and item name username
    And set status value enable in cognito user

  @IGNORE
  Scenario: BCPRXRP-852-BCPRXRP-886-Block user after 3th try failed login [ES]-BCPRI-221
    Given Set edgardo as main test user
    Given Override language to es
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test000000             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | CONTAINS:No pudieron validarse los datos ingresados |
      | errors[0].extensions.description | CONTAINS:Usuario y/o password incorrectos           |
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test111111             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | CONTAINS:No pudieron validarse los datos ingresados |
      | errors[0].extensions.description | CONTAINS:Usuario y/o password incorrectos           |
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Se ha alcanzado el máximo número de intentos permitidos para la autenticación |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: LoginAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value login into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: facundovillalba-uat expected Enable: false
    And Short wait between request
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Su acceso ha sido bloqueando. Favor presionar |
    And delete item dynamoDb by table name LoginAttempts and item name username
    And delete item dynamoDb by table name BlockReason and item name username
    And set status value enable in cognito user

  @Email
  Scenario: BCPRXRP-529 - Restored to Login Attempts when is Success - EN
    Given Set Edgardo2023 as main test user
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test000000             |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message                | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados    |
      | errors[0].extensions.description | OR:If the error persists contact your Credit Union/Username or Password not valid/Usuario y/o password incorrectos |
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: LoginAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 1 into scenario data is equals SCENARIO_DATA:attempts
    #Given perform login using data in graphQL/login.graphql
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | SCENARIO_DATA:PASSWORD |
    Then I print out the results of the response
    Then The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |

  @IGNORE
  Scenario: BCPRXRP-949 - Restored to Login Attempts when is Success - ES
    Given Set Edgardo2023 as main test user
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test000000             |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message                | CONTAINS:No pudieron validarse los datos ingresados |
      | errors[0].extensions.description | CONTAINS:Usuario y/o password incorrectos           |
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: LoginAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 1 into scenario data is equals SCENARIO_DATA:attempts
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | SCENARIO_DATA:PASSWORD |
    Then I print out the results of the response
    Then The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |

  @IGNORE
  Scenario: BCPRXRP-886-Recover Password when blocked cause by LoginAttempts ES
    Given Set edgardo as main test user
    Given Override language to es
    Given perform login using data in graphQL/login.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test000000             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | CONTAINS:No pudieron validarse los datos ingresados |
      | errors[0].extensions.description | CONTAINS:Usuario y/o password incorrectos           |
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test111111             |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].message                | CONTAINS:No pudieron validarse los datos ingresados |
      | errors[0].extensions.description | CONTAINS:Usuario y/o password incorrectos           |
    Given Override language to es
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Se ha alcanzado el máximo número de intentos permitidos para la autenticación |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: LoginAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value login into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: facundovillalba-uat expected Enable: false
    And Short wait between request
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | Test2222222            |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Su acceso ha sido bloqueando. Favor presionar |
    Given post a graphQL request using graphQL/getRegainAccessValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessValidation.status         | CONTAINS:DATA_VALIDATED |
      | data.regainAccessValidation.expires        | NOT NULL                |
      | data.regainAccessValidation.regainAccessId | NOT NULL                |
    Then save response as context variable
    Then save key regainAccessId and path data.regainAccessValidation.regainAccessId as context variable
    Given Set Expression Attribute Values to query
      | String | :regainAccessId | regainAccessId |
      | String | :status_        | DATA_VALIDATED |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: RegainAccessData and filter: regainAccessId = :regainAccessId and #status = :status_ save variables bellow
      | socialSecurityNumber | String |
      | phoneCode            | Number |
      | regainAccessId       | String |
    Given post a graphQL request using graphQL/getRegainAccessCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessCodeValidation.status   | CONTAINS:USER_RECOVERED |
      | data.regainAccessCodeValidation.username | NOT NULL                |
    Given post graphQL request with body graphQL/getSetNewPassword.graphql override table values
      | password | newPass |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | SCENARIO_DATA:newPass  |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user username
    And delete item dynamoDb by table name LoginAttempts and item name username
    And delete item dynamoDb by table name BlockReason and item name username
    And set status value enable in cognito user