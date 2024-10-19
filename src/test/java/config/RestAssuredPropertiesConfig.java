package config;

import static enums.FilesPath.GLOBAL_DATA_FILE_LOCATION;
import com.amazonaws.auth.BasicSessionCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.ApiGatewayException;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.LambdaException;

public class RestAssuredPropertiesConfig {
  private static final Logger log = Logger.getLogger(RestAssuredPropertiesConfig.class);
  private static final Properties prop = new Properties();
  public static String setBaseUri;
  public static String setBaseUriDomain;
  public static String setBaseUriPrivate;
  public static String setCognitoUri;
  public static String bodyData;
  public static String bodyUserData;
  public static String contentType;
  public static String xAmzTarget;
  public static String graphqlEndpoint;
  public static String hostMiddleWare;
  public static String hostMiddleWareProxy;
  public static String accessKeyId;
  public static String secretAccess;
  public static String sessionToken;
  public static String region;
  public static String endpoint;
  public static String language;
  public static String environment;
  public static String environmentMiddleware;
  public static String middlewareEndpoint;
  public static String middlewareProxyEndpoint;
  public static String isMiddlewareProxyActivated;
  public static String isMiddlewareRunDirect;
  public static String[] defaultUser;
  public static String defaultMiddlewareUser;
  public static String clientId;
  public static String userPoolId;
  public static boolean getCognitoUsers;
  public static boolean useCognitoUsers;
  public static String setProxyAddress;
  public static boolean oldPasswordPolice;
  public static boolean useSqsAsNotificationChannel;
  public static boolean useMicroserviceProxyMiddleware;
  public static Integer setProxyPort;
  public static FunctionConfiguration lambdaEnv = null;
  public static Map<String, String> lambdaEnvVariables = new HashMap<>();
  public static ResourcesAWS awsResources;
  public static AwsCredentials awsBasicCredentials;


  public RestAssuredPropertiesConfig() {
    RestAssuredPropertiesConfig.initConfig();
    if(!StringUtils.containsIgnoreCase(getMiddlewareRunDirect(), "true")){
      awsResources = new ResourcesAWS();
      awsBasicCredentials = getAwsBasicCredentials();
      getEnvironmentLambda();
      getEnvironmentConfigurations();
      setClientId();
      setUserPoolId();
      setOldPasswordPolice();
      setUseSqsAsNotificationChannel();
      setUseMicroserviceProxyMiddleware();
      setApiBaseUri();
    }
  }

  public static void initConfig() {
    try {
      InputStream input;
      input = new FileInputStream(GLOBAL_DATA_FILE_LOCATION.getText());
      prop.load(input);
    } catch (IOException e) {
      log.error(e.getMessage());
    }

    environment = StringUtils.isNotEmpty(prop.getProperty("env"))
      ? prop.getProperty("env")
      : null;

    environmentMiddleware = StringUtils.isNotEmpty(prop.getProperty("envMiddleware"))
      ? prop.getProperty("envMiddleware")
      : null;

    setBaseUri = StringUtils.isNotEmpty(String.format("%s.assured.setBaseUri", environment))
            ? prop.getProperty(String.format("%s.assured.setBaseUri", environment))
            : prop.getProperty("assured.setBaseUri");

    setBaseUriDomain = StringUtils.isNotEmpty(String.format("%s.assured.setBaseUriDomain", environment))
            ? prop.getProperty(String.format("%s.assured.setBaseUriDomain", environment))
            : prop.getProperty("assured.setBaseUriDomain");

    setBaseUriPrivate = StringUtils.isNotEmpty(String.format("%s.assured.setBaseUriPrivate", environment))
            ? prop.getProperty(String.format("%s.assured.setBaseUriPrivate", environment))
            : prop.getProperty("assured.setBaseUriPrivate");

    setCognitoUri = StringUtils.isNotEmpty(String.format("%s.assured.cognito", environment))
            ? prop.getProperty(String.format("%s.assured.cognito", environment))
            : prop.getProperty("assured.cognito");

    bodyData = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.bodyData", environment)))
            ? prop.getProperty(String.format("%s.assured.bodyData", environment))
            : prop.getProperty("assured.bodyData");

    bodyUserData = prop.getProperty("assured.bodyUserData");

    contentType = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.content-type", environment)))
            ? prop.getProperty(String.format("%s.assured.content-type", environment))
            : prop.getProperty("assured.content-type");

    xAmzTarget = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.x-amz-target", environment)))
            ? prop.getProperty(String.format("%s.assured.x-amz-target", environment))
            : prop.getProperty("assured.x-amz-target");

    graphqlEndpoint = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.graphqlEndpoint", environment)))
            ? prop.getProperty(String.format("%s.assured.graphqlEndpoint", environment))
            : prop.getProperty("assured.graphqlEndpoint");

    hostMiddleWare = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.hostMW", environmentMiddleware)))
      ? prop.getProperty(String.format("%s.assured.hostMW", environmentMiddleware))
      : prop.getProperty("assured.hostMW");

    hostMiddleWareProxy = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.hostMwProxy", environment)))
            ? prop.getProperty(String.format("%s.assured.hostMwProxy", environment))
            : prop.getProperty("assured.hostMwProxy");

    middlewareEndpoint = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.middlewareEndpoint", environmentMiddleware)))
      ? prop.getProperty(String.format("%s.assured.middlewareEndpoint", environmentMiddleware))
      : prop.getProperty("assured.middlewareEndpoint");

    middlewareProxyEndpoint = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.middlewareProxyEndpoint", environment)))
      ? prop.getProperty(String.format("%s.assured.middlewareProxyEndpoint", environment))
      : prop.getProperty("assured.middlewareProxyEndpoint");

    accessKeyId = StringUtils.isNotEmpty(System.getenv("AWS_ACCESS_KEY_ID"))
            ? System.getenv("AWS_ACCESS_KEY_ID")
            : prop.getProperty("aws.accessKeyId");

    secretAccess = StringUtils.isNotEmpty(System.getenv("AWS_SECRET_ACCESS_KEY"))
                    ? System.getenv("AWS_SECRET_ACCESS_KEY")
                    : prop.getProperty("aws.secretAccess");

    sessionToken = StringUtils.isNotEmpty(System.getenv("AWS_SESSION_TOKEN"))
            ? System.getenv("AWS_SESSION_TOKEN")
            : prop.getProperty("aws.sessionToken");

    region = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.aws.region", environment)))
            ? prop.getProperty(String.format("%s.aws.region", environment))
            : prop.getProperty("aws.region");

    endpoint = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.aws.endpoint", environment)))
            ? prop.getProperty(String.format("%s.aws.endpoint", environment))
            : prop.getProperty("aws.endpoint");

    language = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.aws.language", environment)))
            ? prop.getProperty(String.format("%s.aws.language", environment))
            : prop.getProperty("aws.language");

    isMiddlewareProxyActivated = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.isMiddlewareProxyActivated", environment)))
            ? prop.getProperty(String.format("%s.assured.isMiddlewareProxyActivated", environment))
            : prop.getProperty("assured.isMiddlewareProxyActivated");

    isMiddlewareRunDirect = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.isMiddlewareRunDirect", environment)))
      ? prop.getProperty(String.format("%s.assured.isMiddlewareRunDirect", environment))
      : prop.getProperty("assured.isMiddlewareRunDirect");

    defaultUser = StringUtils.isNotEmpty(prop.getProperty("user"))
            ? prop.getProperty("user").split(",")
            : null;

    defaultMiddlewareUser = StringUtils.isNotEmpty(prop.getProperty("middlewareUser"))
            ? prop.getProperty("middlewareUser")
            : "yuliet";

    clientId = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.clientId", environment)))
            ? prop.getProperty(String.format("%s.assured.clientId", environment))
            : prop.getProperty("assured.clientId");

    userPoolId = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.userPoolId", environment)))
            ? prop.getProperty(String.format("%s.assured.userPoolId", environment))
            : prop.getProperty("assured.userPoolId");

    getCognitoUsers = Boolean.parseBoolean(prop.getProperty("getCognitoUsers"));
    useCognitoUsers = Boolean.parseBoolean(prop.getProperty("useCognitoUsers"));

    setProxyAddress = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.setProxyAddress", environment)))
            ? prop.getProperty(String.format("%s.assured.setProxyAddress", environment))
            : prop.getProperty("assured.setProxyAddress");

    setProxyPort = Integer.valueOf(StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.setProxyPort", environment)))
            ? prop.getProperty(String.format("%s.assured.setProxyPort", environment))
            : prop.getProperty("assured.setProxyPort"));

    oldPasswordPolice = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.oldPasswordPolice", environment)))
            ? Boolean.parseBoolean(prop.getProperty(String.format("%s.assured.oldPasswordPolice", environment)))
            : Boolean.parseBoolean(prop.getProperty("assured.oldPasswordPolice"));

    useSqsAsNotificationChannel = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.useSqsAsNotificationChannel", environment)))
            ? Boolean.parseBoolean(prop.getProperty(String.format("%s.assured.useSqsAsNotificationChannel", environment)))
            : Boolean.parseBoolean(prop.getProperty("assured.useSqsAsNotificationChannel"));

    useMicroserviceProxyMiddleware = StringUtils.isNotEmpty(prop.getProperty(String.format("%s.assured.useMicroserviceProxyMiddleware", environment)))
      ? Boolean.parseBoolean(prop.getProperty(String.format("%s.assured.useMicroserviceProxyMiddleware", environment)))
      : Boolean.parseBoolean(prop.getProperty("assured.useMicroserviceProxyMiddleware"));

  }

  public static AwsCredentials getAwsBasicCredentials() {
    return AwsSessionCredentials.create(
            getAccessKeyId(),
            getSecretAccess(),
            getSessionToken());

  }

  public static BasicSessionCredentials getAwsBasicSessionCredentials() {

    return new BasicSessionCredentials(
            getAccessKeyId(),
            getSecretAccess(),
            getSessionToken());

  }

  public static FunctionConfiguration getEnvironmentLambda() {
    try {
      LambdaClient lambdaClient = createLambdaClient();
      String prefixAWS = awsResources.getPrefix();
      lambdaEnv = findLambdaFunction(lambdaClient, prefixAWS);
    } catch (LambdaException e) {
      logAndThrowSkipException("No AWS credentials charged", e);
    }
    return lambdaEnv;
  }

  private static LambdaClient createLambdaClient() {
    return LambdaClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build();
  }

  private static FunctionConfiguration findLambdaFunction(LambdaClient lambdaClient, String prefixAWS) {
    if (!lambdaClient.listFunctions().hasFunctions()) {
      logAndThrowSkipException("Lambda client doesn't contain any available functions", null);
    }

    String functionName = String.format("%s-Frontend-API", prefixAWS);
    return lambdaClient.listFunctionsPaginator().functions().stream()
      .filter(func -> StringUtils.equalsIgnoreCase(func.functionName(), functionName))
      .findFirst()
      .orElseThrow(() -> new SkipException("Lambda environment config not retrieved"));
  }

  static void logAndThrowSkipException(String message, Exception e) {
    log.error(message, e);
    throw new SkipException(message);
  }


  public static Map<String, String> getEnvironmentConfigurations(){
    if (lambdaEnv.environment() != null) {

      EnvironmentResponse environmentResponse = lambdaEnv.environment();

      if (environmentResponse.hasVariables()) {
        lambdaEnvVariables = environmentResponse.variables();
      }
    }

    if(lambdaEnvVariables.isEmpty()){
      throw new SkipException("Cannot retrieve Lambda env configurations");
    }
    return lambdaEnvVariables;
  }

  public static String getEnvironment() {
    return environment;
  }

  public static String getEnvironmentMiddleware() {
    return environmentMiddleware;
  }

  public static String getMiddlewareRunDirect() {
    return isMiddlewareRunDirect;
  }

  public String getApiBaseUri() {
    String env = awsResources.getPrefix();
    ApiGatewayClient apiGatewayClient = createApiGatewayClient();

    try {
      RestApi restApi = findRestApi(apiGatewayClient, env);
      return buildEndpoint(restApi, env);
    } catch (ApiGatewayException e) {
      logAndThrowSkipException("Error accessing API Gateway", e);
    }

    return "";
  }

  private ApiGatewayClient createApiGatewayClient() {
    return ApiGatewayClient.builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
      .build();
  }

  private RestApi findRestApi(ApiGatewayClient apiGatewayClient, String env) {
    GetRestApisResponse restApiEnv = apiGatewayClient.getRestApis();
    if (!restApiEnv.hasItems()) {
      logAndThrowSkipException("API Gateway doesn't contain any available APIs", null);
    }

    return restApiEnv.items().stream()
      .filter(func -> org.apache.commons.lang3.StringUtils.containsIgnoreCase(func.name(), String.format("%s-Frontend-API-Gateway", env)))
      .findFirst()
      .orElseThrow(() -> new SkipException("No matching API found in API Gateway"));
  }

  private String buildEndpoint(RestApi restApi, String env) {
    String apiId = restApi.id();
    if (StringUtils.isEmpty(apiId)) {
      logAndThrowSkipException("API ID is empty", null);
    }

    if (StringUtils.equalsIgnoreCase(env, "BCPR-CRT")) {
      String endpoint = setBaseUri + apiId + ".execute-api.us-east-1.amazonaws.com/crt";
      log.info("Environment: {} -> {}"  + env + endpoint);
      return endpoint;
    } else {
      String endpoint = String.format("https://%s.execute-api.us-east-1.amazonaws.com", apiId);
      log.info("Environment: {} -> {} " + env + endpoint);
      return endpoint;
    }
  }

  public String getBaseUri() {
    return setBaseUri;
  }

  public String setApiBaseUri() {
    setBaseUri = getApiBaseUri();
    return setBaseUri;
  }

  public String getBaseUriDomain() { return setBaseUriDomain; }

  public String getBaseUriPrivate() { return setBaseUriPrivate; }

  public String getCognitoUri() {
    return setCognitoUri;
  }

  public String getBodyData() {
    return bodyData;
  }

  public String getBodyUserData() {
    return bodyUserData;
  }

  public String getContentType() {
    return contentType;
  }

  public String getXAmzTarget() {
    return xAmzTarget;
  }

  public String getGraphqlEndpoint() {
    return graphqlEndpoint;
  }

  public String getHostMiddleWare() {
    return hostMiddleWare;
  }

  public String getHostMiddleWareProxy() {
    return hostMiddleWareProxy;
  }

  public static String getAccessKeyId() {
    return accessKeyId;
  }

  public static String getSecretAccess() {
    return secretAccess;
  }

  public static String getSessionToken() {
    return sessionToken;
  }

  public String getRegion() {
    return region;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getLanguage() {
    return language;
  }

  public String getClientId() {
    return clientId;
  }

  public String setClientId() {
    String newClientId = "CLIENT_ID";
    clientId = lambdaEnvVariables.containsKey(newClientId) ? lambdaEnvVariables.get(newClientId) : clientId;
    return clientId;
  }

  public String getUserPoolId() {
    return userPoolId;
  }

  public String setUserPoolId() {
    String poolId = "COGNITO_POOL_ID";
    userPoolId = lambdaEnvVariables.containsKey(poolId) ? lambdaEnvVariables.get(poolId) : userPoolId;
    return userPoolId;
  }

  public String getMiddlewareEndpoint() {
    return middlewareEndpoint;
  }

  public String getMiddlewareProxyEndpoint() {
    return middlewareProxyEndpoint;
  }

  public String getIsMiddlewareProxyActivated() {
    return isMiddlewareProxyActivated;
  }

  public String[] getDefaultUser() {
    return defaultUser;
  }

  public String getDefaultMiddlewareUser() {
    return defaultMiddlewareUser;
  }

  public boolean getCognitoUsers() {
    return getCognitoUsers;
  }

  public boolean useCognitoUsers() {
    return useCognitoUsers;
  }

  public boolean setUseSqsAsNotificationChannel() {
    String useSqsAsNotificationChannelKey = "USE_SQS_AS_NOTIFICATION_CHANNEL";
    useSqsAsNotificationChannel = lambdaEnvVariables.containsKey(useSqsAsNotificationChannelKey) ? Boolean.parseBoolean(lambdaEnvVariables.get(useSqsAsNotificationChannelKey)) : true;
    log.info(String.format("The value of the environment variable %s -> %s", useSqsAsNotificationChannelKey, useSqsAsNotificationChannel));
    return useSqsAsNotificationChannel;
  }

  public boolean hardSetUseSqsAsNotificationChannel(boolean hardFlagSet) {
    useSqsAsNotificationChannel = hardFlagSet;
    return useSqsAsNotificationChannel;
  }

  public boolean hardSetUseMicroserviceProxyMiddleware(boolean hardFlagSet) {
    useMicroserviceProxyMiddleware = hardFlagSet;
    return useMicroserviceProxyMiddleware;
  }

  public boolean getUseSqsAsNotificationChannel() {
    return useSqsAsNotificationChannel;
  }

  public boolean setOldPasswordPolice() {
    String oldPasswordPoliceKey = "OLD_PASSWORD_POLICY_ENABLED";
    oldPasswordPolice = lambdaEnvVariables.containsKey(oldPasswordPoliceKey) ? Boolean.parseBoolean(lambdaEnvVariables.get(oldPasswordPoliceKey)) : false;
    return oldPasswordPolice;
  }

  public boolean setUseMicroserviceProxyMiddleware() {
    String useMicroserviceProxyMiddlewareKey = "IS_MIDDLEWARE_PROXY_SERVICE_ENABLED";
    useMicroserviceProxyMiddleware = lambdaEnvVariables.containsKey(useMicroserviceProxyMiddlewareKey) ? Boolean.parseBoolean(lambdaEnvVariables.get(useMicroserviceProxyMiddlewareKey)) : false;
    return useMicroserviceProxyMiddleware;
  }

  public boolean hardSetOldPasswordPolice(boolean hardPassSet) {
    oldPasswordPolice = hardPassSet;
    return oldPasswordPolice;
  }

  public boolean getOldPasswordPolice() {
    return oldPasswordPolice;
  }

  public Integer getSetProxyPort() {
    return setProxyPort;
  }

  public String getSetProxyAddress() {
    return setProxyAddress;
  }

}
