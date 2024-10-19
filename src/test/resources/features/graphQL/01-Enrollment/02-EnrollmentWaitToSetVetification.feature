@Graphql @IGNORE
Feature:Enrollment wait to set verification code

  @IGNORE
  Scenario: BCPRXRP-672-Get Enrolment wait 360 seconds to set verification code
    Given Set yohara as main test user
    Given post a graphQL request using graphQL/getEnrollmentValidationYohara.graphql
    And I print out the results of the response
    When The response is 200
    And I compare response <Path> show the <Values>
      | data.enrollmentValidation.status       | CONTAINS:DATA_VALIDATED |
      | data.enrollmentValidation.expires      | NOT NULL                |
      | data.enrollmentValidation.enrollmentId | NOT NULL                |
    Then save response as context variable
    Then save key enrollmentId and path data.enrollmentValidation.enrollmentId as context variable
    Given Set Expression Attribute Values to query
      | String | :enrollmentId | enrollmentId     |
      | String | :status_      | DATA_VALIDATED   |
    Given Set Expression Attribute Names to query
      | #status | status |
    Given Retrieve data from scan table: EnrollmentData and filter: enrollmentId = :enrollmentId and #status = :status_ save variables bellow
      | data.socialSecurityNumber | String |
      | phoneCode                 | Number |
      | enrollmentId              | String |
    Then I wait during 360 seconds before send the code
    Given post a graphQL request using graphQL/getCodeValidation.graphql
    And I print out the results of the response
    When The response is 200
    Then I compare response <Path> show the <Values>
      | errors[0].message | CONTAINS:The code entered has expired |

