package test;

import static config.AlarmAWS.isAlarmApproximateNumberOfMessagesVisible;
import static config.QueueAWS.isTrappedMessagesQueue;

import config.ResourcesAWS;
import config.RestAssuredExtension;
import enums.AlarmStatus;
import enums.QueueType;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class AlarmTest {
  public static RestAssuredExtension rest = new RestAssuredExtension();

  @Ignore
  @Test
  public void getAlarmApproximateNumberOfMessagesEmailVisibleCloudWatch() {
    String alarmName = ResourcesAWS.buildAlarmName(QueueType.email);
    String statusValue = AlarmStatus.ALARM.name();
    boolean isAlarmThrow = isAlarmApproximateNumberOfMessagesVisible(alarmName, statusValue);
    Assert.assertTrue(isAlarmThrow, String.format("The alarm name %s is not Throw", alarmName));
  }

  @Ignore
  @Test
  public void getAlarmApproximateNumberOfMessagesSmsVisibleCloudWatch() {
    String alarmName = ResourcesAWS.buildAlarmName(QueueType.sms);
    String statusValue = AlarmStatus.ALARM.name();
    boolean isAlarmThrow = isAlarmApproximateNumberOfMessagesVisible(alarmName, statusValue);
    Assert.assertTrue(isAlarmThrow, String.format("The alarm name %s is not Throw", alarmName));
  }

  @Test
  public void getMssAlarmApproximateNumberOfMessagesEmailVisibleCloudWatch() {
    String alarmName = ResourcesAWS.buildAlarmMssName(QueueType.email);
    String statusValue = AlarmStatus.ALARM.name();
    boolean isAlarmThrow = isAlarmApproximateNumberOfMessagesVisible(alarmName, statusValue);
    Assert.assertTrue(isAlarmThrow, String.format("The alarm name %s is not Throw", alarmName));
  }

  @Test
  public void getMssAlarmApproximateNumberOfMessagesSmsVisibleCloudWatch() {
    String alarmName = ResourcesAWS.buildAlarmMssName(QueueType.sms);
    String statusValue = AlarmStatus.ALARM.name();
    boolean isAlarmThrow = isAlarmApproximateNumberOfMessagesVisible(alarmName, statusValue);
    Assert.assertTrue(isAlarmThrow, String.format("The alarm name %s is not Throw", alarmName));
  }

  @Test
  public void validateMessagesSmsVisibleIntoDeadLetterQueue() {
    String queueType="SMS";
    String nameQueue = "deadLetter queue";
    int value = 0;
    String microservicesName = "MSS";
    boolean isTrappedMessage= isTrappedMessagesQueue(microservicesName, queueType,nameQueue,value);
    Assert.assertFalse(isTrappedMessage,String.format("The MSS notification queue %s is message available and messages in flight in %s",queueType,nameQueue));
  }

  @Test
  public void validateMessagesEmailVisibleIntoDeadLetterQueue() {
    String queueType="EMAIL";
    String nameQueue = "deadLetter queue";
    int value = 0;
    String microservicesName = "MSS";
    boolean isTrappedMessage=isTrappedMessagesQueue(microservicesName, queueType,nameQueue,value);
    Assert.assertFalse(isTrappedMessage,String.format("The MSS notification queue %s is message available and messages in flight in %s",queueType,nameQueue));
  }
}
