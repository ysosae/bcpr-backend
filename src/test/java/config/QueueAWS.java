package config;

import static config.ServicesClientAWS.sqsClient;

import com.amazonaws.services.sqs.model.AmazonSQSException;
import enums.QueueType;
import enums.ResourceAWS;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@Setter
@Getter
public class QueueAWS {
  private static final Logger log = Logger.getLogger(QueueAWS.class);
  private String name;
  private static ServicesClientAWS servicesClientAWS= new ServicesClientAWS();

  public QueueAWS() {
  }

  public static boolean existMessagesTrapped(int messagesAvailable, int messagesInFlight) {
    if (messagesAvailable == 0 && messagesInFlight == 0) {
      log.info(
        String.format("No messages available %s and No messages in flight %s", messagesAvailable,
          messagesInFlight));
      return true;
    } else {
      log.info(
        String.format("%s messages available or %s messages in flight are in the queues to be send",
          messagesAvailable, messagesInFlight));
      return false;
    }
  }

  public static boolean existMessagesTrapped(int messagesAvailable, int messagesInFlight,
                                             int value) {
    if (messagesAvailable == value && messagesInFlight == value) {
      log.info(
        String.format("No messages available %s and No messages in flight %s", messagesAvailable,
          messagesInFlight));
      return true;
    } else {
      log.info(
        String.format("%s messages available or %s messages in flight are in the queues to be send",
          messagesAvailable, messagesInFlight));
      return false;
    }
  }

  public static List<String> getListQueues(SqsClient sqsClient) {
    List<String> listQueues;
    try {
      ListQueuesResponse listQueuesResponse = sqsClient.listQueues();
      listQueues = listQueuesResponse.queueUrls();
      return listQueues;
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new SkipException("The List queue is Empty");
    }

  }

  public static String getNameQueues(List<String> getListQueues, String queue) {
    for (String nameQueue : getListQueues) {
      if (StringUtils.containsIgnoreCase(nameQueue, queue)) {
        log.info(
          String.format("The name queues %s exist into List of Simple Queue Services", queue));
        return nameQueue;
      }
    }
    return null;
  }

  public static void getListQueues(List<String> getListQueues) {
    if (!getListQueues.isEmpty()) {
      for (String nameQueue : getListQueues) {
        log.info(nameQueue);
      }
    }
  }

  public static GetQueueAttributesResponse getQueueAttributesResponse(String queueUrl,
                                                                      SqsClient sqsClient) {
    GetQueueAttributesResponse response = null;
    try {
      GetQueueAttributesRequest request = GetQueueAttributesRequest.builder()
        .queueUrl(queueUrl)
        .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
          QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE)
        .build();
      response = sqsClient.getQueueAttributes(request);
    } catch (Exception e) {
      log.error("The get QueueAttribute is not exist");
    }
    return response;
  }

  public static GetQueueAttributesResponse getAllQueueAttributesResponse(String queueUrl,
                                                                         SqsClient sqsClient) {
    GetQueueAttributesResponse response = null;
    try {
      GetQueueAttributesRequest request = GetQueueAttributesRequest.builder()
        .queueUrl(queueUrl)
        .attributeNames(QueueAttributeName.ALL)
        .build();
      response = sqsClient.getQueueAttributes(request);
    } catch (Exception e) {
      log.error("The get QueueAttribute is not exist");
    }
    return response;
  }

  public static String getValueQueueAttributeResponse(GetQueueAttributesResponse response,
                                                      QueueAttributeName queueAttributeName) {
    try {
      return response.attributes().get(queueAttributeName);
    } catch (Exception e) {
      log.error("The get QueueAttribute is not exist");
    }
    return "";
  }

  public static String getArnQueueSqS(String nameQueue) {
    try {
      GetQueueAttributesResponse queueAttribute;
      queueAttribute = QueueAWS.getAllQueueAttributesResponse(nameQueue, servicesClientAWS.getSqsClient());
      return getValueQueueAttributeResponse(queueAttribute, QueueAttributeName.QUEUE_ARN);
    } catch (Exception e) {
      log.error("The get QueueAttribute is not exist");
    }
    return "";
  }

  public static String getArnQueueDeadLetterEmail() {
    List<String> listQueues;
    String longNameQueue;
    String arnDeadLetterEmail = "";
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue =
        QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueueDeadLetter(QueueType.email));
      arnDeadLetterEmail = getArnQueueSqS(longNameQueue);
      log.info("ARN QueueDeadLetterEmail -> " + arnDeadLetterEmail);
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
    return arnDeadLetterEmail;
  }

  public static String getArnQueueEmail() {
    List<String> listQueues;
    String longNameQueue;
    String arnQueueEmail="";
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue = QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueue(QueueType.email));
      arnQueueEmail = getArnQueueSqS(longNameQueue);
      log.info("ARN Queue Email -> " + arnQueueEmail);
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
    return arnQueueEmail;
  }

  public static String getUrlQueueEmail() {
    List<String> listQueues;
    String urlQueueEmail="";
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      urlQueueEmail = QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildMSSQueue(QueueType.email));
      log.info("URL Queue Email -> " + urlQueueEmail);
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
    return urlQueueEmail;
  }

  public static String getUrlQueueSms() {
    List<String> listQueues;
    String urlQueueSms="";
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      urlQueueSms = QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildMSSQueue(QueueType.sms));
      log.info("URL Queue SMS -> " + urlQueueSms);
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
    return urlQueueSms;
  }

  public static boolean isTrappedMessagesQueue(String microservicesName, String queueType, String nameQueue, int value) {
    SqsClient sqsClient = null;
    boolean isTrappedDeadLetterQueueEmail = false;

    try {
      sqsClient = sqsClient();
      String queueUrl = buildQueueUrl(microservicesName, queueType, nameQueue);
      log.info("The queue name is " + queueUrl);

      List<String> listQueues = QueueAWS.getListQueues(sqsClient);
      String queueName = QueueAWS.getNameQueues(listQueues, queueUrl);

      GetQueueAttributesResponse responseQueueAttributes = QueueAWS.getQueueAttributesResponse(queueName, sqsClient);
      int messagesAvailable = QueueAWS.getMessagesAvailableQueues(responseQueueAttributes);
      int messagesInFlight = QueueAWS.getMessagesInFlightQueues(responseQueueAttributes);

      isTrappedDeadLetterQueueEmail = QueueAWS.existMessagesTrapped(messagesAvailable, messagesInFlight, value);
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    } finally {
      if (sqsClient != null) {
        QueueAWS.closeServiceSQsClient(sqsClient);
      }
    }

    return isTrappedDeadLetterQueueEmail;
  }

  private static String buildQueueUrl(String microservicesName, String queueType, String nameQueue) {
    String queueUrl;
    if (StringUtils.containsIgnoreCase(microservicesName, "MSS")) {
      queueUrl = buildMSSQueueUrl(nameQueue, queueType);
    } else {
      queueUrl = buildQueueUrl(nameQueue, queueType);
    }
    return queueUrl;
  }

  private static String buildMSSQueueUrl(String nameQueue, String queueType) {
    if (StringUtils.containsIgnoreCase(nameQueue, ResourceAWS.deadletter.name())) {
      return ResourcesAWS.buildMSSQueueDeadLetter(QueueType.valueOf(queueType.toLowerCase(Locale.ROOT)));
    } else {
      return ResourcesAWS.buildMSSQueue(QueueType.valueOf(queueType.toLowerCase(Locale.ROOT)));
    }
  }

  private static String buildQueueUrl(String nameQueue, String queueType) {
    if (StringUtils.containsIgnoreCase(nameQueue, ResourceAWS.deadletter.name())) {
      return ResourcesAWS.buildQueueDeadLetter(QueueType.valueOf(queueType.toLowerCase(Locale.ROOT)));
    } else {
      return ResourcesAWS.buildQueue(QueueType.valueOf(queueType.toLowerCase(Locale.ROOT)));
    }
  }


  public static int getMessagesAvailableQueues(GetQueueAttributesResponse responseQueueAttributes) {
    return Integer.parseInt(
      responseQueueAttributes.attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));
  }

  public static int getMessagesInFlightQueues(GetQueueAttributesResponse responseQueueAttributes) {
    return Integer.parseInt(responseQueueAttributes.attributes()
      .get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE));
  }

  public static void closeServiceSQsClient(SqsClient sqsClient) {
    sqsClient.close();
  }

}
