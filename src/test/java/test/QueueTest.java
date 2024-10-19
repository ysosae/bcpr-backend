package test;

import static config.QueueAWS.getArnQueueSqS;
import static config.QueueAWS.getUrlQueueEmail;
import static config.QueueAWS.getUrlQueueSms;
import static config.RestAssuredExtension.getBodyFromResource;
import static org.testng.Assert.assertFalse;
import static test.EnvBCPRTest.servicesClientAWS;

import com.amazonaws.services.sqs.model.AmazonSQSException;
import common.CommonLambdaKeyValueConstant;
import config.QueueAWS;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import enums.QueueType;
import java.util.List;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public class QueueTest {
  private static final Logger log = Logger.getLogger(QueueTest.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static String queueDeadLetter;
  public static String queue;

  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1086-Validate the messages into Deadletter queue Email is re-send to comun queue")
  public void validateMicroserviceQueueNoMessageDeadLetterSMS() {
    List<String> listQueues;
    GetQueueAttributesResponse queueAttribute;
    String longNameQueue;
    int messagesAvailable;
    int messagesInFlight;
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue =
        QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueueDeadLetter(QueueType.sms));
      queueAttribute = QueueAWS.getQueueAttributesResponse(longNameQueue, servicesClientAWS.getSqsClient());
      messagesAvailable = QueueAWS.getMessagesAvailableQueues(queueAttribute);
      messagesInFlight = QueueAWS.getMessagesInFlightQueues(queueAttribute);

      Assert.assertTrue(QueueAWS.existMessagesTrapped(messagesAvailable, messagesInFlight),
        "Exists messages available or messages in flight are in the queues to be send");

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
  }
  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1089-Validate the messages into Deadletter queue Email is re-send to comun queue")
  public void validateMicroserviceQueueNoMessageDeadLetterEmail() {
    List<String> listQueues;
    GetQueueAttributesResponse queueAttribute;
    String longNameQueue;
    int messagesAvailable;
    int messagesInFlight;
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue =
        QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueueDeadLetter(QueueType.email));
      queueAttribute = QueueAWS.getQueueAttributesResponse(longNameQueue, servicesClientAWS.getSqsClient());
      messagesAvailable = QueueAWS.getMessagesAvailableQueues(queueAttribute);
      messagesInFlight = QueueAWS.getMessagesInFlightQueues(queueAttribute);

      Assert.assertTrue(QueueAWS.existMessagesTrapped(messagesAvailable, messagesInFlight),
        "Exists messages available or messages in flight are in the queues to be send");

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
  }
  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-862-Validate After 10 failed attempts is recived in Deadletter queue SMS")
  public void validateMicroserviceQueueNoMessageSMS() {
    List<String> listQueues;
    GetQueueAttributesResponse queueAttribute;
    String longNameQueue;
    int messagesAvailable;
    int messagesInFlight;
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue = QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueue(QueueType.sms));
      queueAttribute = QueueAWS.getQueueAttributesResponse(longNameQueue, servicesClientAWS.getSqsClient());
      messagesAvailable = QueueAWS.getMessagesAvailableQueues(queueAttribute);
      messagesInFlight = QueueAWS.getMessagesInFlightQueues(queueAttribute);

      Assert.assertTrue(QueueAWS.existMessagesTrapped(messagesAvailable, messagesInFlight),
        "Exists messages available or messages in flight are in the queues to be send");

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
  }
  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1087-Validate After 10 failed attempts is recived in Deadletter queue Email with spaces time more long")
  public void validateMicroserviceQueueNoMessageEmail() {
    List<String> listQueues;
    GetQueueAttributesResponse queueAttribute;
    String longNameQueue;
    int messagesAvailable;
    int messagesInFlight;
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      longNameQueue = QueueAWS.getNameQueues(listQueues, ResourcesAWS.buildQueue(QueueType.email));
      queueAttribute = QueueAWS.getQueueAttributesResponse(longNameQueue, servicesClientAWS.getSqsClient());
      messagesAvailable = QueueAWS.getMessagesAvailableQueues(queueAttribute);
      messagesInFlight = QueueAWS.getMessagesInFlightQueues(queueAttribute);

      Assert.assertTrue(QueueAWS.existMessagesTrapped(messagesAvailable, messagesInFlight),
        "Exists messages available or messages in flight are in the queues to be send");

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
  }
  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1088-Validate time when Deadletter queue Email is clean")
  public void getMicroserviceDeadLetterQueueSQS_Email() {
    queueDeadLetter = ResourcesAWS.buildQueueDeadLetter(QueueType.email);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesMessageDNSYError.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queueDeadLetter)
        .messageBody(messageBody)
        .build();
      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queueDeadLetter, messageId));
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }
  @Ignore
  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-911-Validate time when Deadletter queue SMS is clean")
  public void getMicroserviceDeadLetterQueueSQS_SMS() {
    queueDeadLetter = ResourcesAWS.buildQueueDeadLetter(QueueType.sms);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesMessageDNSYError.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queueDeadLetter)
        .messageBody(messageBody)
        .build();

      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queueDeadLetter, messageId));

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1239-getMSSDeadLetterQueueSQS_Email")
  public void getMSSDeadLetterQueueSQS_Email() {
    queueDeadLetter = ResourcesAWS.buildMSSQueueDeadLetter(QueueType.email);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesMessageDNSYError.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queueDeadLetter)
        .messageBody(messageBody)
        .build();
      log.info(requestSend.toString());
      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      log.info(requestSend.toString());
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queueDeadLetter, messageId));
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1240-getMSSDeadLetterQueueSQS_SMS")
  public void getMSSDeadLetterQueueSQS_SMS() {
    queueDeadLetter = ResourcesAWS.buildMSSQueueDeadLetter(QueueType.sms);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesSMSMessageDNSYError.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queueDeadLetter)
        .messageBody(messageBody)
        .build();
      log.info(requestSend.toString());
      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      log.info(response.toString());
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queueDeadLetter, messageId));

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1241-getMSSQueueSQS_Email")
  public void getMSSQueueSQS_Email() {
    queue = ResourcesAWS.buildMSSQueue(QueueType.email);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesMessageDNSY.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queue)
        .messageBody(messageBody)
        .build();
      log.info(requestSend.toString());
      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      log.info(response.toString());
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queue, messageId));
    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1242-getMSSQueueSQS_SMS")
  public void getMSSQueueSQS_SMS() {
    queue = ResourcesAWS.buildMSSQueue(QueueType.sms);
    SendMessageRequest requestSend;
    try {
      String messageBody = getBodyFromResource("queuesSMSMessageDNSY.json");
      requestSend = SendMessageRequest
        .builder().
        queueUrl(queue)
        .messageBody(messageBody)
        .build();
      log.info(requestSend.toString());
      SendMessageResponse response = servicesClientAWS.getSqsClient().sendMessage(requestSend);
      log.info(response.toString());
      String messageId = response.messageId();
      assertFalse(messageId.isEmpty(), "The message ID it's not received");
      log.info(String.format("The queue %s: Message ID %s", queueDeadLetter, messageId));

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error(e.getMessage());
      log.error("Path is invalid");
    }
  }

  @Ignore
  @Test
  public void getAllListQueueSQS() {
    List<String> listQueues;
    try {
      listQueues = QueueAWS.getListQueues(servicesClientAWS.getSqsClient());
      QueueAWS.getListQueues(listQueues);

    } catch (AmazonSQSException e) {
      log.error(e.getMessage());
    } catch (NullPointerException e) {
      log.error("Path is invalid");
      throw new SkipException(e.getMessage());
    }
  }

  @Ignore
  @Test
  public void getValueArnQueueDeadLetterEmail() {
    List<String> listQueues;
    String longNameQueue;
    String arnDeadLetterEmail;
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
  }

  @Ignore
  @Test
  public void getValueArnQueueEmail() {
    List<String> listQueues;
    String longNameQueue;
    String arnQueueEmail;
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
  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1246-getValueUrlQueueSmsMSS")
  public void getValueUrlQueueSmsMSS() {
    Assert.assertEquals(getUrlQueueSms(), CommonLambdaKeyValueConstant.SQS_NOTIFICATION_SMS_QUEUE);

  }

  @Test(
    groups = {"QueueTest"},
    testName = "BCPRXRP-1245-getValueUrlQueueEmailMSS")
  public void getValueUrlQueueEmailMSS() {
    Assert.assertEquals( getUrlQueueEmail(), CommonLambdaKeyValueConstant.SQS_NOTIFICATION_EMAIL_QUEUE);
  }
}
