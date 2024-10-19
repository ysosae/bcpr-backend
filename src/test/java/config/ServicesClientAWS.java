package config;

import static config.RestAssuredPropertiesConfig.awsBasicCredentials;
import static config.RestAssuredPropertiesConfig.getAwsBasicCredentials;
import static config.RestAssuredPropertiesConfig.getAwsBasicSessionCredentials;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.AmazonECSException;
import com.amazonaws.services.neptunedata.model.S3Exception;
import com.amazonaws.services.s3.AmazonS3;
import software.amazon.awssdk.services.codepipeline.CodePipelineClient;
import software.amazon.awssdk.services.codepipeline.model.CodePipelineException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.sns.SnsClient;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.ApiGatewayException;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CloudFrontException;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ElasticLoadBalancingV2Exception;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sqs.SqsClient;


public class ServicesClientAWS {
  private static final Logger log = Logger.getLogger(ServicesClientAWS.class);
  private static AmazonS3 s3Client = null;
  private static LambdaClient lambdaClient = null;
  private static CloudFrontClient cloudFrontClient = null;
  private static AmazonECS ecsClient = null;
  private static SqsClient sqsClient = null;
  private static CloudWatchClient cloudWatchClient = null;
  private static ApiGatewayClient apiGatewayClient = null;
  private static CognitoIdentityProviderClient cognitoIdentityProviderClient = null;
  private static ElasticLoadBalancingV2Client elasticLoadBalancingV2Client = null;
  private static SnsClient snsClient = null;
  private static SecretsManagerClient secretsManagerClient = null;
  private static CodePipelineClient codePipelineClient = null;

  public ServicesClientAWS() {
    awsBasicCredentials = getAwsBasicCredentials();
    s3Client = s3Client();
    lambdaClient = lambdaClient();
    cloudFrontClient = cloudFrontClient();
    ecsClient = ecsClient();
    sqsClient = sqsClient();
    cloudWatchClient  = cloudWatchClient();
    apiGatewayClient = apiGatewayClient();
    cognitoIdentityProviderClient = cognitoIdentityProviderClient();
    elasticLoadBalancingV2Client = elasticLoadBalancingV2Client();
    snsClient=snsClient();
    secretsManagerClient = secretsManagerClient();
    codePipelineClient = codePipelineClient();
  }

  public AmazonS3 getS3Client() {
    return s3Client;
  }

  public SecretsManagerClient getSecretsManagerClient() {
    return secretsManagerClient;
  }

  public  AmazonECS getEcsClient() {
    return ecsClient;
  }


  public  LambdaClient getLambdaClient() {
    return lambdaClient;
  }

  public  CloudFrontClient getCloudFrontClient() {
    return cloudFrontClient;
  }

  public  CodePipelineClient getCodePipelineClient() {
    return codePipelineClient;
  }

  public  SqsClient getSqsClient() {
    return sqsClient;
  }


  public  ApiGatewayClient getApiGatewayClient() {
    return apiGatewayClient;
  }

  public  CognitoIdentityProviderClient getCognitoIdentityProviderClient() {
    return cognitoIdentityProviderClient;
  }
  public  ElasticLoadBalancingV2Client getElasticLoadBalancingV2Client() {
    return elasticLoadBalancingV2Client;
  }

  public  SnsClient getSnsClientClient() {
    return snsClient;
  }

  public static AmazonS3 s3Client() {
    try {
      s3Client = AmazonS3ClientBuilder.standard()
        .withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();
    } catch (S3Exception e) {
      log.error("Cannot connect with AmazonS3Client, reason: " + e.getMessage());
    }
    return s3Client;
  }

  public static LambdaClient lambdaClient(){
    try {
      lambdaClient=LambdaClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();

    }catch (LambdaException e){
      log.error("Cannot connect with AmazonLambdaClient, reason: " + e.getMessage());
    }
    return lambdaClient;
  }

  public static CloudFrontClient cloudFrontClient(){
    try{
      cloudFrontClient = CloudFrontClient.builder()
        .region(Region.AWS_GLOBAL)
        .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();

    }catch (CloudFrontException e){
      log.error("Cannot connect with AmazonCloudFrontClient, reason: " + e.getMessage());
    }
    return cloudFrontClient;
  }

  public static AmazonECS ecsClient(){
    try {
      ecsClient = AmazonECSClientBuilder.standard().
        withRegion(Region.US_EAST_1.toString())
        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
        .build();

    }catch (AmazonECSException e){
      log.error("Cannot connect with AmazonECSClient, reason: " + e.getMessage());
    }
    return ecsClient;
  }

  public static SqsClient sqsClient(){
    try{
      sqsClient = SqsClient.builder()
        .region(Region.US_EAST_1).credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();
    }catch (AmazonSQSException | NullPointerException e){
      log.error("Cannot connect with AmazonSQsClient, reason: " + e.getMessage());
    }
    return sqsClient;
  }

  public static CloudWatchClient cloudWatchClient(){
    try {
       cloudWatchClient = CloudWatchClient.builder()
        .region(Region.US_EAST_1).credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();
    }catch (CloudWatchException e){
      log.error("Cannot connect with AmazonCloudWatchClient, reason:" + e.getMessage());
    }
    return cloudWatchClient;
  }

  public static ApiGatewayClient apiGatewayClient(){
    try {
       apiGatewayClient = ApiGatewayClient.builder()
        .region(Region.US_EAST_1).credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();
    }catch (ApiGatewayException e){
      log.error("Cannot connect with AmazonApiGatewayClient, reason:" + e.getMessage());
    }
    return apiGatewayClient;
  }

public static CognitoIdentityProviderClient cognitoIdentityProviderClient (){
    try {
      cognitoIdentityProviderClient =
        CognitoIdentityProviderClient.builder()
          .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
          .build();
    }catch (CognitoIdentityProviderException e){
      log.error("Cannot connect with AmazonCognitoIdentityProvider, reason:" + e.getMessage());
    }
  return cognitoIdentityProviderClient;

}

  public static ElasticLoadBalancingV2Client elasticLoadBalancingV2Client (){
    try {
      elasticLoadBalancingV2Client =
        ElasticLoadBalancingV2Client.builder()
          .region(Region.US_EAST_1)
          .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
          .build();
    }catch (ElasticLoadBalancingV2Exception e){
      log.error("Cannot connect with Elastic Load Balancer client, reason:" + e.getMessage());
    }
    return elasticLoadBalancingV2Client;
  }

  public static SnsClient snsClient(){
    try {
      snsClient=SnsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();

    }catch (SnsException e){
      log.error("Cannot connect with AmazonSns, reason: " + e.getMessage());
    }
    return snsClient;
  }

  public static SecretsManagerClient secretsManagerClient(){
    try {
      secretsManagerClient=SecretsManagerClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();

    }catch (SecretsManagerException e){
      log.error("Cannot connect with Amazon Secret Manager, reason: " + e.getMessage());
    }
    return secretsManagerClient;
  }

  public static CodePipelineClient codePipelineClient(){
    try {
      codePipelineClient=CodePipelineClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
        .build();

    }catch (CodePipelineException e){
      log.error("Cannot connect with Amazon Code Pipeline, reason: " + e.getMessage());
    }
    return codePipelineClient;
  }

}
