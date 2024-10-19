package config;

import static config.ServicesClientAWS.cloudWatchClient;
import static test.EnvBCPRTest.displayErrorList;

import com.amazonaws.services.ecs.model.KeyValuePair;
import common.CommonAlarmStatusReason;
import enums.AlarmStatus;
import enums.QueueType;
import enums.ResourceAWS;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DescribeAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricAlarm;

public class AlarmAWS {
  private static final Logger log = Logger.getLogger(AlarmAWS.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static List<KeyValuePair> errorList;

  public static boolean isAlarmApproximateNumberOfMessagesVisible(String alarmName,
                                                                  String statusValue) {

    boolean result = false;
    CloudWatchClient cloudWatchClient = cloudWatchClient();

    errorList = new ArrayList<>();
    DescribeAlarmsResponse describeAlarmsResponse;
    try {
      describeAlarmsResponse = cloudWatchClient.describeAlarms();
      List<MetricAlarm> alarmList = describeAlarmsResponse.metricAlarms();

      for (MetricAlarm alarm : alarmList) {
        if (StringUtils.containsIgnoreCase(alarm.alarmName(), alarmName)) {
          if (StringUtils.equalsIgnoreCase(alarm.stateValueAsString(), statusValue) &&
            StringUtils.containsIgnoreCase(alarm.stateReason(),
              CommonAlarmStatusReason.ALARM_STATUS_REASON)
          ) {
            result = true;
            if (StringUtils.containsIgnoreCase(alarm.alarmName(), alarmName)) {
              if (StringUtils.equalsIgnoreCase(alarm.stateValueAsString(), statusValue)) {
                result = StringUtils.containsIgnoreCase(alarm.stateReason(),
                  "was greater than or equal to the threshold (1.0).");
                log.info("************** ALARM **********");
                log.info("Alarm Name: " + alarm.alarmName());
                log.info("State: " + alarm.stateValue());
                log.info("Reason: " + alarm.stateReason());
                break;
              }
            } else if (
              StringUtils.equalsIgnoreCase(alarm.stateValueAsString(), AlarmStatus.OK.name()) ||
                StringUtils.equalsIgnoreCase(alarm.stateValueAsString(),
                  AlarmStatus.INSUFFICIENT_DATA.name())) {
              KeyValuePair alarmReason = new KeyValuePair();
              alarmReason.setName(alarm.alarmName());
              alarmReason.setValue(alarm.stateValueAsString());
              errorList.add(alarmReason);
            }
          }
          displayErrorList();
        }

      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return result;
  }

  public static QueueType getQueueType(String queue){
    QueueType queueType = null;
    if(StringUtils.equalsIgnoreCase(queue, QueueType.sms.name())){
      queueType= QueueType.sms;
    }
    if(StringUtils.equalsIgnoreCase(queue,QueueType.email.name())){
      queueType = QueueType.email;
    }
    return queueType;

  }

  public static String getStatusAlarm(String status){
    String statusValue = "";
    if(StringUtils.equalsIgnoreCase(status, AlarmStatus.ALARM.name())){
      statusValue = AlarmStatus.ALARM.name();
    }else if(StringUtils.equalsIgnoreCase(status, AlarmStatus.OK.name())){
      statusValue = AlarmStatus.OK.name();
    }else if(StringUtils.equalsIgnoreCase(status, AlarmStatus.INSUFFICIENT_DATA.name())){
      statusValue = AlarmStatus.INSUFFICIENT_DATA.name();

    }
    return statusValue;
  }

  public static String buildAlarmNameByResource(String microservicesName, QueueType queueType) {
    String alarmName = "";
    if (StringUtils.isEmpty(microservicesName)) {
      alarmName = ResourcesAWS.buildAlarmMssName(queueType);
    }
    if (StringUtils.containsIgnoreCase(microservicesName, ResourceAWS.Application.name())) {
      alarmName = ResourcesAWS.buildAlarmName(queueType);
    }
    if (StringUtils.containsIgnoreCase(microservicesName, ResourceAWS.MSS.name())) {
      alarmName = ResourcesAWS.buildAlarmMssName(queueType);
    }
    return alarmName;
  }
}
