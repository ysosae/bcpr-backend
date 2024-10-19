@Graphql @attempts @notification
Feature: Card Activation

  Background:
    Given Set AdminAndIsCreditCardExpiredFalse as main test user
    Given post a graphQL request using graphQL/adminCardDeactivationMutation.graphql
    Then I print out the results of the response
    Then The response code is 200
    And I compare middleware response <Path> show the <Values>
      | data.adminCardDeactivation.status | SUCCESS
    And Short wait between request

  @Email
  @ActivateCard
  Scenario: BCPRXRP-874-BCPRXRP-878 - block user SSN after 3th try Card Activation failed EN
    Given Set AdminAndIsCreditCardExpiredFalse as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | SCENARIO_DATA:wrongLastEightDigits |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | SCENARIO_DATA:wrongLastEightDigits |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | SCENARIO_DATA:wrongLastEightDigits |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | OR:The max attempts to activate a card was reached/Se ha alcanzado el máximo numero de intentos permitidos para activar la tarjeta |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value card-activation into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: yoharasantana expected Enable: false
    And delete item dynamoDb by table name CardActivationAttempts and item name username
    And delete item dynamoDb by table name BlockReason and item name username
    And set status value enable in cognito user

  @IGNORE
  Scenario: BCPRXRP-875-BCPRXRP-875 - block user after 3th try Card Activation failed [ES]
    Given Override language to es
    Given Set Yohara as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345647 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados/¡Acceso denegado. |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345648 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados/¡Acceso denegado. |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345649 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Se ha alcanzado el máximo numero de intentos permitidos para activar la tarjeta |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value card-activation into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: yoharasantana expected Enable: false
    And delete item dynamoDb by table name CardActivationAttempts and item name username
    And delete item dynamoDb by table name BlockReason and item name username
    And set status value enable in cognito user

  @IGNORE
  Scenario: BCPRXRP-531 - block user TAX_ID after 3th try Card Activation failed EN
    Given Set Yuliet as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345647 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados/¡Acceso denegado. |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345648 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados/¡Acceso denegado. |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345649 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:Se ha alcanzado el máximo numero de intentos permitidos para activar la tarjeta |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | yulietsosa-uat |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Retrieve data from scan cognito list filter attribute username and user: yulietsosa-uat expected Enable: false

  @IGNORE
  Scenario: BCPRXRP-530-878 - Recover Password when blocked cause by CardActivationAttempts
    Given Set Yohara as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345647 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345648 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345649 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:The max attempts to activate a card was reached |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value card-activation into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: yoharasantana expected Enable: false
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
      | PASSWORD | Test12345              |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user username
#    Given Delete user username from Cognito
#    And clean token of username user

  @IGNORE
  Scenario: BCPRXRP-876- 877- Recover Password when blocked cause by CardActivationAttempts - ES
    Given Override language to es
    Given Set Yohara as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345647 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345648 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | 12345649 |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].extensions.description | CONTAINS:The max attempts to activate a card was reached |
    And Short wait between request
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 3 into scenario data is equals SCENARIO_DATA:attempts
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: BlockReason and filter: #username = :username save variables bellow
      | blockReason | String |
    Then expected value card-activation into scenario data is equals SCENARIO_DATA:blockReason
    Given Retrieve data from scan cognito list filter attribute username and user: yoharasantana expected Enable: false
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
      | PASSWORD | Test12345              |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
    And Wait 5 seconds between request
    Then validate LastPassword entry in DynamoDB for user username
#    Given Delete user username from Cognito
#    And clean token of username user


  @IGNORE
  Scenario: BCPRXRP-531 - RegainAccess recover blocked user TAX_ID
    Given post a graphQL request using graphQL/getRegainAccessValidationYuliet.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.regainAccessValidation.status         | CONTAINS:DATA_VALIDATED |
      | data.regainAccessValidation.expires        | NOT NULL                |
      | data.regainAccessValidation.regainAccessId | NOT NULL                |
    Then save response as context variable
    Then save key regainAccessId and path data.regainAccessValidation.regainAccessId as context variable
    Given post a graphQL request using graphQL/getResendRegainAccessCode.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.resendRegainAccessCode.status         | CONTAINS:CODE_RESENT |
      | data.resendRegainAccessCode.regainAccessId | NOT NULL             |
    Given Set Expression Attribute Values to query
      | String | :ssn            | 581135033   |
      | String | :status_        | CODE_RESENT |
      | String | :expirationCode | Today       |
      | String | :type_          | TAX_ID      |
    Given Set Expression Attribute Names to query
      | #status | status |
      | #type   | type   |
    Given Retrieve data from scan table: RegainAccessData and filter: socialSecurityNumber = :ssn and #status = :status_ and codeExpiration >= :expirationCode and #type >= :type_ save variables bellow
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
      | password | Test1234567 |
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.setNewPassword.status | CONTAINS:PASSWORD_UPDATE |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | yulietsosa-uat |
      | PASSWORD | Test1234567    |
    When The response is 200
    And I print out the results of the response
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |

  @Email
  @ActivateCard
  Scenario: BCPRXRP-529 - Restored to Card Activation Attempts when is Success
    Given Set AdminAndIsCreditCardExpiredFalse as main test user
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | lastEightDigits | SCENARIO_DATA:wrongLastEightDigits |
    Then I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | LIST:errors.message | OR:The data provided is incorrect/Access denied!/An unexpected error/No pudieron validarse los datos ingresados |
    Given Set Expression Attribute Values to query
      | String | :username | nameUsername |
    Given Set Expression Attribute Names to query
      | #username | username |
    Given Retrieve data from scan table: CardActivationAttempts and filter: #username = :username save variables bellow
      | attempts | Number |
    Then expected value 1 into scenario data is equals SCENARIO_DATA:attempts
    Given post graphQL request with body graphQL/getCardActivationMutation.graphql override table values
      | LastEightDigits | lastEightDigits |
    Then I print out the results of the response
    Then The response is 200
    And I compare response <Path> show the <Values>
      | data.cardActivation.status  | SUCCESS                                               |
      | data.cardActivation.message | OR:Successfully activated card ending/The card ending |
    Given post graphQL request with body graphQL/login.graphql override table values
      | USERNAME | SCENARIO_DATA:username |
      | PASSWORD | SCENARIO_DATA:password |
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | data.login | NOT NULL |
