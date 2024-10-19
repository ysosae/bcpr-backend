package test;

import static config.ResourcesAWS.buildTopicsSnsName;
import static config.RestAssuredPropertiesConfig.awsResources;
import static test.EnvBCPRTest.getEnvLambdas;
import static test.EnvBCPRTest.validateValueIf;

import common.CommonLambdaRecoverLostMessagesConstant;
import config.QueueAWS;
import config.RestAssuredExtension;
import config.ServicesClientAWS;
import enums.ProtocolSubscription;
import enums.QueueType;
import enums.ResourceAWS;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.ListEventSourceMappingsRequest;
import software.amazon.awssdk.services.lambda.model.ListEventSourceMappingsResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsResponse;
import software.amazon.awssdk.services.sns.model.Subscription;

public class TriggerTest {
  private static final Logger log = Logger.getLogger(ConfigurationTest.class);
  public static RestAssuredExtension rest = new RestAssuredExtension();
  public static ServicesClientAWS servicesClientAWS = new ServicesClientAWS();
  public static LambdaClient lambdaClient = servicesClientAWS.getLambdaClient();
  public static SnsClient snsClient = servicesClientAWS.getSnsClientClient();
  static String functionLambdaNameAWS =
    awsResources.setFunctionLambdaName(ResourceAWS.Lambdas, ResourceAWS.RecoverLostMessages);

  @Test
  public void validateEnvironmentVariablesLambdaRecoverLostMessages() {
    EnvironmentResponse environmentResponse = getEnvLambdas(functionLambdaNameAWS);
    if (environmentResponse.hasVariables()) {
      Map<String, String> variables = environmentResponse.variables();

      for (Map.Entry<String, String> var : variables.entrySet()) {
        log.info(var);
      }
      validateValueIf(variables, CommonLambdaRecoverLostMessagesConstant.DLQ_URL,
        QueueAWS.getArnQueueDeadLetterEmail());

      validateValueIf(variables, CommonLambdaRecoverLostMessagesConstant.ORIGINAL_QUEUE_URL,
        QueueAWS.getArnQueueEmail());

    }
  }

  @Test
  public void getTriggersIntoLambdas() {
    String buildTopics= buildTopicsSnsName(QueueType.email);
    String protocol = ProtocolSubscription.lambda.toString();
    List<Subscription> listSubscriptionsResponse = listSubscription();
    Subscription subscriptionObj = getSubscriptionSnsByAttribute(buildTopics, protocol);
    log.info("TOPIC ARN TRIGGER: " + getTopicArnSnS(subscriptionObj));

    for (Subscription subscription : listSubscriptionsResponse) {
      log.info("Subscription ARN: " + subscription.subscriptionArn());
      log.info("Protocol: " + subscription.protocol());
      log.info("Endpoint: " + subscription.endpoint());
      log.info("Owner: " + subscription.owner());
      log.info("TopicArn: " + subscription.topicArn());
    }

    ListEventSourceMappingsRequest listEventSourceMappingsRequest =
      ListEventSourceMappingsRequest.builder()
        .functionName(functionLambdaNameAWS)
        .build();

    ListEventSourceMappingsResponse listEventSourceMappingsResponse =
      lambdaClient.listEventSourceMappings(listEventSourceMappingsRequest);

    listEventSourceMappingsResponse.eventSourceMappings().forEach(mapping -> {
      log.info("Trigger (SNS Topic): " + mapping.eventSourceArn());
      log.info("Lambda Function ARN: " + mapping.functionArn());
      log.info("Statement ID: " + mapping.state());
      log.info("Subscription ARN: " + mapping.uuid());
    });
  }

  public static Subscription getSubscriptionSnsByAttribute(String subscriptionArn,
                                                           String protocol) {
    ListSubscriptionsResponse response = snsClient.listSubscriptions();
    List<Subscription> listSubscriptionsResponse = response.subscriptions();
    return listSubscriptionsResponse.stream().filter(func ->
        StringUtils.containsIgnoreCase(func.subscriptionArn(),
          subscriptionArn) && StringUtils.equalsIgnoreCase(func.protocol(),
          protocol)).findFirst()
      .orElse(null);
  }

  public static String getTopicArnSnS(Subscription subscription) {
    return subscription.topicArn();
  }

  public static List<Subscription> listSubscription() {
    return snsClient.listSubscriptions().subscriptions();
  }

}
