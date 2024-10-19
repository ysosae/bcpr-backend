package config;



import static config.RestAssuredPropertiesConfig.getAwsBasicCredentials;

import enums.DomainUrl;
import enums.DynamoDBTable;
import enums.ExtensionTemplate;
import enums.ProjectName;
import enums.ProtocolUrl;
import enums.QueueType;
import enums.ResourceAWS;
import java.util.Locale;
import lombok.Getter;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;

@Getter
public class ResourcesAWS {
  private final String prefix;
  private final String env;
  private final String templateBucket;
  private final String databaseBucket;
  private final String claimBucket;
  private final String bucketS3ImageClaimsName;
  private final String bucketS3ImageName;
  private String functionLambdaName;
  private final String cognitoUrlString;
  private final String iconImageDomain;
  private final String publicImageDomain;
  public static AwsCredentials awsBasicCredentials;
  public static ServicesClientAWS servicesClientAWS=null;

  public ResourcesAWS() {
    servicesClientAWS = new ServicesClientAWS();
    awsBasicCredentials = getAwsBasicCredentials();
    prefix = buildPrefixResources(ProjectName.BCPR);
    env = RestAssuredPropertiesConfig.getEnvironment();
    templateBucket = buildBucketsName(ResourceAWS.templates);
    databaseBucket = buildBucketsName(ResourceAWS.databases);
    claimBucket = buildBucketsName(ResourceAWS.claims);
    functionLambdaName = buildFunctionLambdaName(ResourceAWS.Frontend, ResourceAWS.API);
    bucketS3ImageClaimsName= buildNameBucketsImage(ResourceAWS.claims, ResourceAWS.s3);
    bucketS3ImageName= buildNameBucketsImage(ResourceAWS.image, ResourceAWS.s3);
    cognitoUrlString = buildCognitoUrlString(ProtocolUrl.https, ResourceAWS.cognito.name().concat("-idp"));
    iconImageDomain = buildCloudFrontUrlString(ProtocolUrl.https, ResourceAWS.cloudfront.name(),"d10ilvrdf2l8nr");
    publicImageDomain = buildCloudFrontUrlString(ProtocolUrl.https, ResourceAWS.cloudfront.name(),"d39a5oopdi9t25");
  }


  public String setFunctionLambdaName(ResourceAWS prefix1, ResourceAWS prefix2) {
    functionLambdaName = buildFunctionLambdaName(prefix1, prefix2);
    return functionLambdaName;
  }

  public static String buildPrefixResources(ProjectName prefix) {
    StringBuilder prefixBuilder = new StringBuilder();
    if (!getEnvironment().isEmpty()) {
      prefixBuilder.append(prefix);
      prefixBuilder.append("-");
      prefixBuilder.append(getEnvironment());
    }

    return prefixBuilder.toString();
  }

  public static String buildPrefixBuckets(ProjectName prefix) {
    StringBuilder prefixBuilder = new StringBuilder();
    if (!getEnvironment().isEmpty()) {
      prefixBuilder.append(prefix);
      prefixBuilder.append("-");
      prefixBuilder.append(getEnvironment().toLowerCase(Locale.ROOT));
    }

    return prefixBuilder.toString();
  }

  public static String getEnvironment(){
    if (RestAssuredPropertiesConfig.getEnvironment() != null) {
      return RestAssuredPropertiesConfig.getEnvironment().replaceAll("EVT-","");
    }
    return "";
  }

  public static String buildDynamoDbTableName(DynamoDBTable tableName){

    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      tableName;
  }

  public static String buildBucketsName(ResourceAWS resourcesAWS){

    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      resourcesAWS.name();
  }

  public static String setBuildBucketsName(ResourceAWS resourcesAWS, String env){

    return ProjectName.bcpr +
      "-" +
      env.toLowerCase(Locale.ROOT) +
      "-" +
      resourcesAWS.name();
  }

  public static String buildFunctionLambdaName(ResourceAWS resourcesFirstAWS, ResourceAWS resourcesLastAWS){

    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      resourcesFirstAWS.name() +
      "-" +
      resourcesLastAWS.name();
  }

  public static String buildFunctionLambdaName(ResourceAWS resourcesLastAWS){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
     resourcesLastAWS.name();
  }


  public static String buildNameBucketsImage(ResourceAWS prefix, ResourceAWS resource){

    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      prefix.name() +
      "." +
      resource.name() +
      "." +
      Region.US_EAST_1 +
      "." +
      "amazonaws" +
      "." +
      DomainUrl.com;
  }

  public static String buildCognitoUrlString(ProtocolUrl protocol, String resource){

    return protocol +
      "://" +
      resource +
      "." +
      Region.US_EAST_1 +
      "." +
      "amazonaws" +
      "." +
      DomainUrl.com +
      "/";
  }

  public static String buildCloudFrontUrlString(ProtocolUrl protocol, String resource, String value){
    return protocol +
      "://" +
      value +
      "." +
      resource +
      "." +
      DomainUrl.net;
  }

  public static String buildQueueDeadLetter(QueueType queueType){
    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      ResourceAWS.notification +
      "-" +
      queueType.name() +
      "-" +
      ResourceAWS.deadletter +
      "-" +
      ResourceAWS.queue;
  }

  public static String buildMSSQueueDeadLetter(QueueType queueType){
    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      ResourceAWS.mss +
      "-" +
      queueType.name() +
      "-" +
      ResourceAWS.deadletter +
      "-" +
      ResourceAWS.queue;
  }

  public static String buildQueue(QueueType queueType){
    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      ResourceAWS.notification +
      "-" +
      queueType.name() +
      "-" +
      ResourceAWS.queue;
  }

  public static String buildMSSQueue(QueueType queueType){
    return buildPrefixBuckets(ProjectName.bcpr) +
      "-" +
      ResourceAWS.mss +
      "-" +
      queueType.name() +
      "-" +
      ResourceAWS.queue;
  }

  public static String buildAlarmName(QueueType queueType){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      ResourceAWS.Application +
      "-" +
      "bcprqueuebcpr" +
      getEnvironment().toLowerCase(Locale.ROOT) +
      ResourceAWS.notification +
      queueType.name() +
      ResourceAWS.queue;

  }

  public static String buildAlarmMssName(QueueType queueType){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      ResourceAWS.mss.name().toUpperCase(Locale.ROOT) +
      "-" +
      ResourceAWS.Notification.name() +
      "-" +
      "bcpr" +
      getEnvironment().toLowerCase(Locale.ROOT) +
      ResourceAWS.mss +
      queueType.name() +
      ResourceAWS.queue;

  }

  public static String buildLoadBalancer(){
    return ResourceAWS.internal +
      "-" +
      buildPrefixResources(ProjectName.BCPR) +
      "-" +
      ResourceAWS.microservices +
      "-" +
      ResourceAWS.lb;
  }

  public static String buildEndpointMiddlewareLoadBalancer(String dnsName){
    return ProtocolUrl.http +
      "://" +
      dnsName +
      "/" +
      "v1/middleware-proxy";
  }

  public static String buildExtensionTemplate(){
    return "." +
      ExtensionTemplate.html;
  }

  public static String buildTopicsSnsName(QueueType queueType){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      ResourceAWS.Application +
      "-" +
      "bcprqueuebcpr" +
      getEnvironment().toLowerCase(Locale.ROOT) +
      ResourceAWS.notification +
      queueType.name() +
      ResourceAWS.queue +
      ResourceAWS.topic;
  }

  public static String buildSecretsName(ResourceAWS resourcesFirstAWS){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      resourcesFirstAWS.name();
  }

  public static String buildSecretsName(ResourceAWS resourcesFirstAWS, ResourceAWS resourcesLastAWS){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      resourcesFirstAWS.name() +
      "-" +
      resourcesLastAWS.name();
  }

  public static String buildSecretsName(ResourceAWS resourcesFirstAWS, ResourceAWS resourcesSecondAWS, ResourceAWS resourcesLastAWS){
    return buildPrefixResources(ProjectName.BCPR) +
      "-" +
      resourcesFirstAWS.name() +
      "-" +
      resourcesSecondAWS.name() +
      "-" +
      resourcesLastAWS.name();
  }


}
