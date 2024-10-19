@secondChance @Notification @IGNORE @Alarm
Feature: MC-Notifications-Email - The lambda trigger was Throw after 1, 2 and 5 minutes - ALARM
   @Email
   Scenario: BCPRXRP-1104-The lambda trigger was Throw after 5 minutes
    Given validate alarm of queue Email notification has status value ALARM into Application services
    When I wait during 360 seconds before send the trigger of Alarm
    Then validate Application the notification queue Email into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into queue is message available and messages in flight in 0
    Then validate Application the notification queue Email into queue is message available and messages in flight in 0

  Scenario: BCPRXRP-1223-The lambda trigger was Throw after 2 minutes
    Given validate alarm of queue Email notification has status value ALARM into Application services
    When I wait during 120 seconds before send the trigger of Alarm
    Then validate Application the notification queue Email into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into queue is message available and messages in flight in 0
    Then validate Application the notification queue Email into queue is message available and messages in flight in 0

  Scenario: BCPRXRP-1222-The lambda trigger was Throw after 1 minutes
    Given validate alarm of queue Email notification has status value ALARM into Application services
    When I wait during 60 seconds before send the trigger of Alarm
    Then validate Application the notification queue Email into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into deadLetter queue is message available and messages in flight in 0
    Then validate Application the notification queue SMS into queue is message available and messages in flight in 0
    Then validate Application the notification queue Email into queue is message available and messages in flight in 0

  @Email
  Scenario: BCPRXRP-1255-The lambda EMAIL trigger was Throw after 5 minutes into MSS
    Given validate alarm of queue Email notification has status value ALARM into MSS services
    When I wait during 360 seconds before send the trigger of Alarm
    Then validate MSS the notification queue Email into deadLetter queue contain available message is different to 0
    Then validate MSS the notification queue Email into queue is message available and messages in flight in 0


  @Email
  Scenario: BCPRXRP-1256-The lambda SMS trigger was Throw after 5 minutes into MSS
    Given validate alarm of queue SMS notification has status value ALARM into MSS services
    When I wait during 360 seconds before send the trigger of Alarm
    Then validate MSS the notification queue SMS into deadLetter queue contain available message is different to 0
    Then validate MSS the notification queue SMS into queue is message available and messages in flight in 0
