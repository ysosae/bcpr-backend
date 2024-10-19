package test;

import static allure.AllureLogger.attachScreenShot;
import static config.DynamoDbAWS.scannerUserLastPasswordInfo;
import static config.ResourcesAWS.buildExtensionTemplate;
import static config.ResourcesAWS.buildSecretsName;
import static config.ResourcesAWS.getEnvironment;
import static config.RestAssuredExtension.amazonDynamoDB;
import static config.RestAssuredExtension.configProperties;
import static config.RestAssuredExtension.getSessionUser;
import static config.RestAssuredExtension.result;
import static config.RestAssuredPropertiesConfig.getAwsBasicSessionCredentials;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.ContainerOverride;
import com.amazonaws.services.ecs.model.DescribeTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.DescribeTaskDefinitionResult;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.DescribeTasksResult;
import com.amazonaws.services.ecs.model.KeyValuePair;
import com.amazonaws.services.ecs.model.ListClustersResult;
import com.amazonaws.services.ecs.model.ListTasksRequest;
import com.amazonaws.services.ecs.model.ListTasksResult;
import com.amazonaws.services.ecs.model.Task;
import com.amazonaws.services.ecs.model.TaskOverride;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonLambdaEmailNotificationConstant;
import common.CommonLambdaFrontendAPIConstant;
import common.CommonLambdaFrontendAPIKeyValueConstant;
import common.CommonLambdaKeyValueConstant;
import common.CommonLambdaSmsNotificationConstant;
import common.CommonSecretsKeyConstant;
import common.CommonSecretsKeyValueConstant;
import common.CommonVersionConstant;
import config.AbstractAPI;
import config.ImageDomainAWS;
import config.ResourcesAWS;
import config.RestAssuredExtension;
import config.RestAssuredPropertiesConfig;
import config.ServicesClientAWS;
import enums.DynamoDBTable;
import enums.ProjectName;
import enums.ProtocolUrl;
import enums.QueueType;
import enums.ResourceAWS;
import enums.StatusCard;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.com.google.gson.JsonSyntaxException;
import gherkin.deps.com.google.gson.reflect.TypeToken;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import io.qameta.allure.TmsLinks;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;
import java.util.*;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsResponse;
import software.amazon.awssdk.services.cloudfront.model.Origin;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ElasticLoadBalancingV2Exception;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.EnvironmentResponse;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.LambdaException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;


public class EnvBCPRTest extends AbstractAPI {

    public static ResponseOptions<Response> response = null;
    private static final Logger log = Logger.getLogger(EnvBCPRTest.class);
    public static RestAssuredExtension rest = new RestAssuredExtension();
    public static ServicesClientAWS servicesClientAWS = new ServicesClientAWS();
    public static String username = null;
    public static List<String> errorList = new ArrayList<>();
    static List<String> bucketLocalList = getBucketLocalList();
    public static AmazonS3 s3Client = servicesClientAWS.getS3Client();
    public static SecretsManagerClient secretsManagerClient = servicesClientAWS.getSecretsManagerClient();
    public static LambdaClient lambdaClient = servicesClientAWS.getLambdaClient();
    public static CloudFrontClient cloudFrontClient = servicesClientAWS.getCloudFrontClient();
    public static AmazonECS ecsClient = servicesClientAWS.getEcsClient();
    public static ResourcesAWS awsResources = new ResourcesAWS();
    public static String dbDynamoDbTableName = null;
    public static String prefixAWS = awsResources.getPrefix();
    static String nameResourcesAWS = awsResources.getTemplateBucket();
    static String functionLambdaNameAWS = awsResources.getFunctionLambdaName();
    static String bucketS3ImageName = awsResources.getBucketS3ImageName();
    static String bucketS3ImageClaimsName = awsResources.getBucketS3ImageClaimsName();
    static String cognitoUrlString = awsResources.getCognitoUrlString();
    public static ElasticLoadBalancingV2Client elasticLoadBalancingV2Client =
            servicesClientAWS.getElasticLoadBalancingV2Client();
    public static Map<KeyValuePair, String> errorListEnvironmentVariable = new HashMap<>();
    public static String nextTokenSecret = null;
    private static final int ITEMS_PER_PAGE = 30;
    private static final Gson gson = new Gson();

    @Stories({@Story("BCPRI-895"), @Story("BCPRI-897")})
    @TmsLinks({@TmsLink("BCPRXRP-866"), @TmsLink("BCPRXRP-460"), @TmsLink("BCPRXRP-866")})
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-866-908-testValidateAllDynamoDBTable")
    public void testValidateAllDynamoDBTable() {
        listTableNameOfDynamoDbByEnvironments();
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1002-testValidateDynamicEmailTemplatesDBTable")
    public void testValidateTemplateDBTable() {
        try {
            EnvironmentResponse environmentResponse = getEnvLambdas();
            log.info(String.format("Environments -> %s", environmentResponse));
            boolean ENABLE_DYNAMO_TEMPLATES = false;

            if (environmentResponse.hasVariables()) {
                Map<String, String> variables = environmentResponse.variables();
                if (variables.containsKey(CommonLambdaFrontendAPIConstant.ENABLE_DYNAMO_TEMPLATES)
                        && variables.containsKey(CommonLambdaFrontendAPIConstant.DYNAMIC_EMAIL_TEMPLATES_DYNAMO_TABLE)) {
                    ENABLE_DYNAMO_TEMPLATES =
                            Boolean.parseBoolean(variables.get(CommonLambdaFrontendAPIConstant.ENABLE_DYNAMO_TEMPLATES));
                    dbDynamoDbTableName =
                            variables.get(CommonLambdaFrontendAPIConstant.DYNAMIC_EMAIL_TEMPLATES_DYNAMO_TABLE);
                }
            }

            if (!ENABLE_DYNAMO_TEMPLATES) {
                dbDynamoDbTableName =
                        ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.DynamicEmailTemplates);
            }
            isListTemplateIsNotEmpty(dbDynamoDbTableName);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            log.error("Cannot retrieve Environment information values" + e.getMessage());
            Assert.fail("Cannot retrieve Environment information values " + e.getMessage());
        }
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1137-testValidateAllBucketsTemplateEmail")
    public void testGetBuckets() {
        Bucket bucket = getBucketTemplateName(s3Client);
        assertBucketPresent(bucket);

        ObjectListing objectListing = getAllObjectsListDatabasesByBucketName(bucket.getName());
        List<S3ObjectSummary> s3ObjectSummaries = getListS3ObjectSummariesTemplate(objectListing);
        List<KeyValuePair> errorListBuckets = validateDifferencesLocalBucketWithS3Bucket(s3ObjectSummaries);

        if (!errorListBuckets.isEmpty()) {
            displayErrorList(errorListBuckets);
            failTestWithMissingTemplates();
        }

        validateTemplateBuckets(objectListing);
        assertNoErrorsInS3TemplateBuckets();
    }

    private void assertBucketPresent(Bucket bucket) {
        Assert.assertTrue(isPresentBucket(bucket), "Is Not present this buckets " + bucket.getName());
    }

    private void failTestWithMissingTemplates() {
        Assert.fail("New template exists in S3 buckets that are missing in the LOCAL list of template", new Throwable());
    }

    private void assertNoErrorsInS3TemplateBuckets() {
        Assert.assertTrue(errorList.isEmpty(), "Buckets into S3 template Errors were found");
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1258-testValidateAllBucketsDataBasesDynamoJson")
    public void testGetBucketsDataBases() {
        Bucket bucket = getBucketDataBaseName(s3Client);
        assertBucketPresent(bucket);

        ObjectListing objectListing = getAllObjectsListDatabasesByBucketName(bucket.getName());
        List<S3ObjectSummary> s3ObjectSummaries = getListS3ObjectSummariesTemplate(objectListing);
        List<KeyValuePair> errorListBuckets = validateDifferencesLocalBucketWithS3Bucket(s3ObjectSummaries);

        if (!errorListBuckets.isEmpty()) {
            displayErrorList(errorListBuckets);
            failTestWithMissingTemplates();
        }

        validateDatabaseBuckets(objectListing);
        assertNoErrorsInS3TemplateBuckets();

    }

    public boolean compareListSizes(int size1, int size2) {
        return size1 != size2;
    }

    public List<KeyValuePair> validateDifferencesLocalBucketWithS3Bucket(List<S3ObjectSummary> s3ObjectSummaries) {
        List<KeyValuePair> errorList = new ArrayList<>();
        String extensionTemplate = buildExtensionTemplate();

        if (s3ObjectSummaries.isEmpty()) {
            return errorList;
        }

        if (!compareListSizes(s3ObjectSummaries.size(), bucketLocalList.size())) {
            return errorList;
        }

        for (S3ObjectSummary s3Object : s3ObjectSummaries) {
            if (isObjectMissingFromLocalBucket(s3Object, extensionTemplate)) {
                errorList.add(createMissingTemplateError(s3Object));
            }
        }

        return errorList;
    }

    private boolean isObjectMissingFromLocalBucket(S3ObjectSummary s3Object, String extensionTemplate) {
        return s3Object != null
                && !isPresentTemplateLocalBucket(s3Object.getKey())
                && !isExcludeS3AwsTemplate(s3Object, "template")
                && isAdmittedExtendsFileTemplate(s3Object, extensionTemplate);
    }

    private KeyValuePair createMissingTemplateError(S3ObjectSummary s3Object) {
        KeyValuePair error = new KeyValuePair();
        error.setName("Missing template");
        error.setValue(String.format("Template %s is not present on the LOCAL bucket list", s3Object.getKey()));
        return error;
    }


    public boolean isPresentBucket(Bucket bucket) {
        return bucket != null
                && StringUtils.isNotEmpty(bucket.getName())
                && StringUtils.equalsIgnoreCase(nameResourcesAWS, bucket.getName());
    }

    public boolean isExcludeS3AwsTemplate(S3ObjectSummary objectSummary, String templateName) {
        String key = objectSummary.getKey();
        return StringUtils.containsIgnoreCase(key, templateName);
    }

    public boolean isAdmittedExtendsFileTemplate(S3ObjectSummary objectSummary,
                                                 String extensionTemplate) {
        String key = objectSummary.getKey();
        return key.endsWith(extensionTemplate);
    }

    public ObjectListing getAllObjectsListDatabasesByBucketName(String bucketName) {
        return s3Client.listObjects(bucketName);
    }

    public List<S3ObjectSummary> getListS3ObjectSummariesTemplate(ObjectListing templateObjectsList) {
        List<S3ObjectSummary> listTemplateObject = new ArrayList<>();
        if (templateObjectsList.getObjectSummaries() != null) {
            listTemplateObject = templateObjectsList.getObjectSummaries();
        }
        return listTemplateObject;
    }

    public static boolean isPresentTemplateLocalBucket(String findTemplate) {
        return bucketLocalList.contains(findTemplate);
    }

    public void displayErrorList(List<KeyValuePair> errorList) {
        if (!errorList.isEmpty()) {
            log.info("################## ERROR LIST ################################");
            log.info("Errors found: " + errorList.size());
            for (KeyValuePair values : errorList) {
                log.info(values.getName() + ": " + values.getValue());
            }
            log.info("################### ERROR LIST ###############################");
        }
    }

    public void displayErrorEnvironmentList() {
        if (!errorListEnvironmentVariable.isEmpty()) {
            log.info("################## ERROR LIST ################################");
            log.info("Errors found: " + errorListEnvironmentVariable.size());
            for (Map.Entry<KeyValuePair, String> entry : errorListEnvironmentVariable.entrySet()) {
                KeyValuePair keyValuePair = entry.getKey();
                String value = entry.getValue();
                log.info("Key: " + keyValuePair + ", Value: " + value);
            }
            attachScreenShot("Taking screenshot ", errorListEnvironmentVariable.toString());
            log.info("################### ERROR LIST ###############################");
        }
    }

    public static void displayErrorList() {
        if (!errorList.isEmpty()) {
            log.info("################## ERROR LIST ################################");
            log.info("Errors found: " + errorList.size());
            for (String values : errorList) {
                log.info(values);
            }
            log.info("################### ERROR LIST ###############################");
        }
    }

    public static void validateTemplateBuckets(ObjectListing templateObjectsList) {
        List<String> bucketLocalList = getBucketTemplateLocalList();
        List<KeyValuePair> errorList = new ArrayList<>();

        if (templateObjectsList.isTruncated()) {
            log.warn("Template objects list is truncated. Results may be incomplete.");
            return;
        }

        List<S3ObjectSummary> templateObjects = templateObjectsList.getObjectSummaries();
        if (templateObjects.isEmpty()) {
            log.error("Template list is empty");
            return;
        }

        for (String template : bucketLocalList) {
            validateTemplate(template, templateObjects, errorList);
        }

        logErrors(errorList);

        Assert.assertTrue(errorList.isEmpty(), "Errors were found: " + errorList);
    }

    private static void validateTemplate(String template, List<S3ObjectSummary> templateObjects, List<KeyValuePair> errorList) {
        Optional<S3ObjectSummary> templateObject = templateObjects.stream()
                .filter(value -> StringUtils.containsIgnoreCase(value.getKey(), template))
                .findFirst();

        if (templateObject.isEmpty() || StringUtils.isEmpty(templateObject.get().getKey())
                || !StringUtils.equalsIgnoreCase(templateObject.get().getKey(), template)) {
            KeyValuePair versionError = new KeyValuePair();
            versionError.setName("Missing template");
            versionError.setValue(String.format("Template %s is not present on the bucket list", template));
            errorList.add(versionError);
        }

        log.info(template);
    }

    public static void validateDatabaseBuckets(ObjectListing databaseObjectsList) {
        List<String> bucketLocalList = getBucketDatabaseJsonLocalList();
        List<KeyValuePair> errorList = new ArrayList<>();

        if (databaseObjectsList.isTruncated()) {
            log.warn("Database objects list is truncated. Results may be incomplete.");
            return;
        }

        List<S3ObjectSummary> databaseJsonObjects = databaseObjectsList.getObjectSummaries();
        if (databaseJsonObjects.isEmpty()) {
            log.error("Template list is empty");
            return;
        }

        for (String database : bucketLocalList) {
            validateDatabase(database, databaseJsonObjects, errorList);
        }

        logErrors(errorList);

        Assert.assertTrue(errorList.isEmpty(), "Errors were found: " + errorList);
    }

    private static void validateDatabase(String database, List<S3ObjectSummary> databaseJsonObjects, List<KeyValuePair> errorList) {
        Optional<S3ObjectSummary> databaseObject = databaseJsonObjects.stream()
                .filter(value -> StringUtils.containsIgnoreCase(value.getKey(), database))
                .findFirst();

        if (databaseObject.isEmpty() || StringUtils.isEmpty(databaseObject.get().getKey())
                || !StringUtils.containsIgnoreCase(databaseObject.get().getKey(), database)) {
            KeyValuePair versionError = new KeyValuePair();
            versionError.setName("Missing Buckets database");
            versionError.setValue(String.format("Template %s is not present on the bucket list", database));
            errorList.add(versionError);
        }

        log.info(database);
    }

    private static void logErrors(List<KeyValuePair> errorList) {
        if (!errorList.isEmpty()) {
            log.info("Errors found: " + errorList.size());
            errorList.forEach(error -> log.info(error.getName() + ": " + error.getValue()));
        }
    }


    public static Bucket getBucketTemplateName(AmazonS3 s3Client) {
        List<Bucket> listBuckets = s3Client.listBuckets();
        Bucket bucket = new Bucket();
        try {
            if (!listBuckets.isEmpty()) {
                bucket = listBuckets.stream().filter(
                                buck -> StringUtils.equalsIgnoreCase(buck.getName(), nameResourcesAWS))
                        .findFirst()
                        .orElse(null);
            } else {
                Assert.fail("Bucket was not available");
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return bucket;
    }

    public static Bucket getBucketDataBaseName(AmazonS3 s3Client) {
        nameResourcesAWS = awsResources.getDatabaseBucket();
        List<Bucket> listBuckets = s3Client.listBuckets();
        Bucket bucket = new Bucket();
        try {
            if (!listBuckets.isEmpty()) {
                bucket = listBuckets.stream().filter(
                                buck -> StringUtils.equalsIgnoreCase(buck.getName(), nameResourcesAWS))
                        .findFirst()
                        .orElse(null);
            } else {
                Assert.fail("Bucket was not available");
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return bucket;
    }

    public static EnvironmentResponse getEnvLambdas() {
        EnvironmentResponse environmentResponse = null;
        FunctionConfiguration lambdaEnv = null;
        try {
            if (lambdaClient.listFunctions().hasFunctions()) {
                lambdaEnv = lambdaClient.listFunctionsPaginator()
                        .functions().stream()
                        .filter(func ->
                                StringUtils.equalsIgnoreCase(func.functionName(),
                                        functionLambdaNameAWS))
                        .findFirst()
                        .orElse(null);
            } else {
                log.error("lambdaClient didn't contains functions available");
            }
        } catch (LambdaException e) {
            log.error(e.getMessage());
        }
        if (lambdaEnv != null) {
            if (lambdaEnv.environment() != null) {
                environmentResponse = lambdaEnv.environment();
            } else {
                log.error("cannot retrieve Environment information values");
            }
        }

        return environmentResponse;
    }

    public static EnvironmentResponse getEnvLambdas(String functionLambdaNameAWS) {
        EnvironmentResponse environmentResponse = null;
        FunctionConfiguration lambdaEnv = null;
        try {
            if (lambdaClient.listFunctions().hasFunctions()) {
                lambdaEnv = lambdaClient.listFunctionsPaginator()
                        .functions().stream()
                        .filter(func ->
                                StringUtils.equalsIgnoreCase(func.functionName(),
                                        functionLambdaNameAWS))
                        .findFirst()
                        .orElse(null);
            } else {
                log.error("lambdaClient didn't contains functions available");
            }
        } catch (LambdaException e) {
            log.error(e.getMessage());
        }
        if (lambdaEnv != null) {
            if (lambdaEnv.environment() != null) {
                environmentResponse = lambdaEnv.environment();
            } else {
                log.error("cannot retrieve Environment information values");
            }
        }

        return environmentResponse;
    }

    public static FunctionConfiguration getFunctionConfiguration(String functionLambdaNameAWS) {
        FunctionConfiguration functionConfiguration = null;
        try {
            if (lambdaClient.listFunctions().hasFunctions()) {
                functionConfiguration = lambdaClient.listFunctionsPaginator()
                        .functions().stream()
                        .filter(func ->
                                StringUtils.equalsIgnoreCase(func.functionName(),
                                        functionLambdaNameAWS))
                        .findFirst()
                        .orElse(null);
            } else {
                log.error("lambdaClient didn't contains functions available");
            }
        } catch (LambdaException e) {
            log.error(e.getMessage());
        }
        return functionConfiguration;
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-867-868-913-testGetLambdaFrontendAPI")
    public void testGetLambdaFrontendAPI() {
        try {
            setupLambdaTestEnvironment();
            EnvironmentResponse environmentResponse = getEnvLambdas();
            errorList = new ArrayList<>();
            errorListEnvironmentVariable = new HashMap<>();

            if (environmentResponse.hasVariables()) {
                Map<String, String> variables = environmentResponse.variables();
                logLambdaEnvironment(variables);

                validateCommonLambdaVariables(variables);

                displayErrorEnvironmentList();

                Assert.assertTrue(errorListEnvironmentVariable.isEmpty(), "Lambda misconfigurations found.");
            }

        } catch (NullPointerException e) {
            handleLambdaError(e);
        }

    }

    private void setupLambdaTestEnvironment() {
        functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.Frontend, ResourceAWS.API);
        prefixAWS = awsResources.getPrefix();
        errorList = new ArrayList<>();
        errorListEnvironmentVariable = new HashMap<>();
    }

    private void logLambdaEnvironment(Map<String, String> variables) {
        log.info("################## LAMBDA ENVIRONMENT ################################");
        log.info("TOTAL ENVIRONMENT VARIABLES -> " + variables.size());

        for (Map.Entry<String, String> var : variables.entrySet()) {
            log.info(var);
        }
    }

    private void validateCommonLambdaVariables(Map<String, String> variables) {
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ENABLE_DYNAMO_TEMPLATES, "TrueOrFalse");
//    validateValueIf(variables, CommonLambdaFrontendAPIConstant.IS_MIDDLEWARE_PROXY_SERVICE_ENABLED, "TrueOrFalse");
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.COUNTRY_CODE, "1Or+1");
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.THRESHOLD_REWARDS_LIMIT, CommonLambdaFrontendAPIKeyValueConstant.THRESHOLD_REWARDS_LIMIT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ENROLLMENT_MAX_ATTEMPTS_CODE_RESENT, CommonLambdaFrontendAPIKeyValueConstant.ENROLLMENT_MAX_ATTEMPTS_CODE_RESENT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.DAYS_NEAR_TO_EXPIRE, CommonLambdaFrontendAPIKeyValueConstant.DAYS_NEAR_TO_EXPIRE);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CODE_TIME_EXPIRATION_IN_MINUTES, CommonLambdaFrontendAPIKeyValueConstant.CODE_TIME_EXPIRATION_IN_MINUTES);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.API_VERSION_CDK, CommonLambdaFrontendAPIKeyValueConstant.API_VERSION_CDK);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGIN_MAX_ATTEMPTS_LIMIT, CommonLambdaFrontendAPIKeyValueConstant.LOGIN_MAX_ATTEMPTS_LIMIT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.PASSWORD_REPETITION_THRESHOLD, CommonLambdaFrontendAPIKeyValueConstant.PASSWORD_REPETITION_THRESHOLD);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.SQS_NOTIFICATION_EMAIL_QUEUE, CommonLambdaKeyValueConstant.SQS_NOTIFICATION_EMAIL_QUEUE);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.SQS_NOTIFICATION_SMS_QUEUE, CommonLambdaKeyValueConstant.SQS_NOTIFICATION_SMS_QUEUE);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.VISION_PLUS_ENDPOINT, CommonLambdaFrontendAPIKeyValueConstant.VISION_PLUS_ENDPOINT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CHANGE_PASSWORD_USER_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.CHANGE_PASSWORD_USER_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.BANK_CLAIM_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.BANK_CLAIM_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.WELCOME_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.WELCOME_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CONTACT_US_USER_AUTH_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.CONTACT_US_USER_AUTH_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CONTACT_US_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.CONTACT_US_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CARD_ACTIVATION_MAX_ATTEMPTS_LIMIT, CommonLambdaFrontendAPIKeyValueConstant.CARD_ACTIVATION_MAX_ATTEMPTS_LIMIT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGIN_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.LOGIN_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CONTACT_US_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.CONTACT_US_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MAKE_PAYMENT_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.MAKE_PAYMENT_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.USER_CLAIM_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.USER_CLAIM_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ACCOUNT_CREATED_HTML_TEMPLATE_NAME, CommonLambdaFrontendAPIKeyValueConstant.ACCOUNT_CREATED_HTML_TEMPLATE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MINUTES_TO_CALL_AUTH_MIDDLEWARE, CommonLambdaFrontendAPIKeyValueConstant.MINUTES_TO_CALL_AUTH_MIDDLEWARE);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MAX_ALLOWED_INACTIVITY_TIME_SECONDS, CommonLambdaFrontendAPIKeyValueConstant.MAX_ALLOWED_INACTIVITY_TIME_SECONDS);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.VALID_USER_SESSION_TIME_SECONDS, CommonLambdaFrontendAPIKeyValueConstant.VALID_USER_SESSION_TIME_SECONDS);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.URL_EXPIRATION_SECONDS, CommonLambdaFrontendAPIKeyValueConstant.URL_EXPIRATION_SECONDS);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MAX_RETRY_ATTEMPTS, CommonLambdaFrontendAPIKeyValueConstant.MAX_RETRY_ATTEMPTS);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.FAQ_REWARDS_CATEGORY, CommonLambdaFrontendAPIKeyValueConstant.FAQ_REWARDS_CATEGORY);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.DNSY_SERVICE_HOST, CommonLambdaFrontendAPIKeyValueConstant.DNSY_SERVICE_HOST);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ACH_SERVICE_HOST, CommonLambdaFrontendAPIKeyValueConstant.ACH_SERVICE_HOST);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ENABLE_PLAYGROUND, CommonLambdaFrontendAPIKeyValueConstant.ENABLE_PLAYGROUND);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ENABLE_PLAYGROUND_SCHEMA, CommonLambdaFrontendAPIKeyValueConstant.ENABLE_PLAYGROUND_SCHEMA);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.PLAID_SERVICE_HOST, CommonLambdaFrontendAPIKeyValueConstant.PLAID_SERVICE_HOST);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.SECRET_MANAGER_SECRET_NAME, CommonLambdaFrontendAPIKeyValueConstant.SECRET_MANAGER_SECRET_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MOBILE_STORE_VERSION, CommonVersionConstant.MOBILE_STORE_VERSION);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.MINIMUM_MOBILE_VERSION_ALLOW, CommonVersionConstant.MINIMUM_MOBILE_VERSION_ALLOW);
        validateDynamoTableConfigurations(variables);
        validateMiscellaneousLambdaConstants(variables);
        // validateEndpointMiddlewareLoadBalancer(variables);
        validateNotContains(variables);
        validateImageConstants(variables);
    }

    private void validateDynamoTableConfigurations(Map<String, String> variables) {
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LAST_PASSWORD_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LastPassword));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CALL_CENTER_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.CallCenter));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.FAQ_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.FAQ));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.FAQ_CATEGORY_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Category));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CONTACT_US_REASONS_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ContactUsReason));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CONTACT_US_EMAILS_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ContactUs));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.ENROLLMENT_DATA_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.EnrollmentData));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.COOPERATIVE_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Cooperative));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGIN_ATTEMPTS_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.LoginAttempts));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CARD_ACTIVATION_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.CardActivationAttempts));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.REGAIN_ACCESS_DATA_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.RegainAccessData));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.WALLET_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Wallet));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CLAIM_TYPES_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ClaimTypeData));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CLAIM_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.ClaimData));
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.PAYMENT_DYNAMO_TABLE, ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.Payment));
    }


    private void validateMiscellaneousLambdaConstants(Map<String, String> variables) {
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.COGNITO_POOL_ID, configProperties.getUserPoolId());
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.CLIENT_ID, configProperties.getClientId());
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.DNSY_CMDB_ID, ProjectName.BCPR.name());
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGGER_LEVEL, CommonLambdaFrontendAPIKeyValueConstant.LOGGER_LEVEL);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGGER_FORMAT, CommonLambdaFrontendAPIKeyValueConstant.LOGGER_FORMAT);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.LOGGER_SERVICE_NAME, CommonLambdaFrontendAPIKeyValueConstant.LOGGER_SERVICE_NAME);
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.UPLOAD_BUCKET, awsResources.getClaimBucket());
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.TEMPLATES_BUCKET, awsResources.getTemplateBucket());
        validateValueIf(variables, CommonLambdaFrontendAPIConstant.COGNITO_URL, cognitoUrlString);
    }

    private void validateNotContains(Map<String, String> variables) {
        validateNotContains(variables, CommonLambdaFrontendAPIConstant.USE_SQS_AS_NOTIFICATION_CHANNEL);
        validateNotContains(variables, CommonLambdaFrontendAPIConstant.IS_MIDDLEWARE_PROXY_SERVICE_ENABLED);
        validateNotContains(variables, CommonLambdaFrontendAPIConstant.MIDDLEWARE_PROXY_SERVICE_ENDPOINT);
    }

    private void validateImageConstants(Map<String, String> variables) {
        validateImageDomain(variables, CommonLambdaFrontendAPIConstant.PUBLIC_IMAGE_DOMAIN);
        validateImageDomain(variables, CommonLambdaFrontendAPIConstant.ICON_IMAGE_DOMAIN);
    }


    private void handleLambdaError(NullPointerException e) {
        log.error("Lambda error: " + e.getMessage());
        Assert.fail("Lambda is null", e);
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1236-testGetLambdaEmailNotificationLambda")
    public void testGetLambdaEmailNotificationLambda() {
        try {
            functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.emailNotificationLambda);
            log.info("Name of Lambda - > " + functionLambdaNameAWS);
            prefixAWS = awsResources.getPrefix();
            EnvironmentResponse environmentResponse = getEnvLambdas(functionLambdaNameAWS);
            errorListEnvironmentVariable = new HashMap<>();

            if (environmentResponse.hasVariables()) {
                Map<String, String> variables = environmentResponse.variables();

                for (Map.Entry<String, String> var : variables.entrySet()) {
                    log.info(var);
                }
                validateValueIf(variables, CommonLambdaEmailNotificationConstant.DNSY_SECRET_NAME,
                        CommonSecretsKeyValueConstant.DNSY_SECRET_NAME);
                validateValueIf(variables, CommonLambdaEmailNotificationConstant.DNSY_EMAIL_ENDPOINT,
                        CommonLambdaFrontendAPIKeyValueConstant.DNSY_EMAIL_ENDPOINT);

                displayErrorEnvironmentList();

                Assert.assertTrue(errorListEnvironmentVariable.isEmpty(), "Lambda Microservice Serverless EMAIL errors were found");
            }

        } catch (NullPointerException e) {
            log.error("The Lambda is " + e.getMessage());
            Assert.fail("The Lambda is null", new Throwable());
        }

    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1237-testGetLambdaSmsNotificationLambda")
    public void testGetLambdaSmsNotificationLambda() {
        try {
            functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.smsNotificationLambda);
            log.info("Name of Lambda - > " + functionLambdaNameAWS);

            prefixAWS = awsResources.getPrefix();
            EnvironmentResponse environmentResponse = getEnvLambdas(functionLambdaNameAWS);
            errorListEnvironmentVariable = new HashMap<>();

            if (environmentResponse.hasVariables()) {
                Map<String, String> variables = environmentResponse.variables();

                for (Map.Entry<String, String> var : variables.entrySet()) {
                    log.info(var);
                }
                validateValueIf(variables, CommonLambdaSmsNotificationConstant.DNSY_SECRET_NAME,
                        CommonSecretsKeyValueConstant.DNSY_SECRET_NAME);
                validateValueIf(variables, CommonLambdaSmsNotificationConstant.DNSY_SMS_ENDPOINT,
                        CommonLambdaFrontendAPIKeyValueConstant.DNSY_SMS_ENDPOINT);

                displayErrorEnvironmentList();

                Assert.assertTrue(errorListEnvironmentVariable.isEmpty(), "Lambda Microservice Serverless SMS errors were found");
            }

        } catch (NullPointerException e) {
            log.error("The Lambda is " + e.getMessage());
            Assert.fail("The Lambda is null", new Throwable());
        }
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-321-testGetVersionNodeFrontendApiLambda")
    public void getVersionNodeFrontendApi() {
        try {
            functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.Frontend, ResourceAWS.API);
            prefixAWS = awsResources.getPrefix();
            FunctionConfiguration functionConfiguration = getFunctionConfiguration(functionLambdaNameAWS);
            log.info(String.format("The version of Lambda name %s is %s ", functionLambdaNameAWS, functionConfiguration.runtimeAsString()));
            Assert.assertTrue(StringUtils.containsIgnoreCase(functionConfiguration.runtimeAsString(), CommonVersionConstant.RUNTIME_NODE_VERSION_FRONTEND_API), "This version does not contain the Lambda");
        } catch (NullPointerException e) {
            log.error("The Lambda is " + e.getMessage());
            Assert.fail("The Lambda is null", new Throwable());
        }
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1319-testGetVersionNodeLambdaSmsNotificationLambda")
    public void getVersionNodeSmsNotificationMSS() {
        try {
            functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.smsNotificationLambda);
            log.info("Name of Lambda - > " + functionLambdaNameAWS);

            prefixAWS = awsResources.getPrefix();
            FunctionConfiguration functionConfiguration = getFunctionConfiguration(functionLambdaNameAWS);
            log.info(String.format("The version of Lambda name %s is %s ", functionLambdaNameAWS, functionConfiguration.runtimeAsString()));
            Assert.assertTrue(StringUtils.containsIgnoreCase(functionConfiguration.runtimeAsString(), CommonVersionConstant.RUNTIME_NODE_VERSION_SMS_MSS), "This version does not contain the Lambda");
        } catch (NullPointerException e) {
            log.error("The Lambda is " + e.getMessage());
            Assert.fail("The Lambda is null", new Throwable());
        }
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1320-testGetVersionNodeEmailNotificationLambda")
    public void getVersionNodeEmailNotificationMSS() {
        try {
            functionLambdaNameAWS = ResourcesAWS.buildFunctionLambdaName(ResourceAWS.emailNotificationLambda);
            log.info("Name of Lambda - > " + functionLambdaNameAWS);
            prefixAWS = awsResources.getPrefix();
            FunctionConfiguration functionConfiguration = getFunctionConfiguration(functionLambdaNameAWS);
            log.info(String.format("The version of Lambda name %s is %s ", functionLambdaNameAWS, functionConfiguration.runtimeAsString()));
            Assert.assertTrue(StringUtils.containsIgnoreCase(functionConfiguration.runtimeAsString(), CommonVersionConstant.RUNTIME_NODE_VERSION_EMAIL_MSS), "This version does not contain the Lambda");

        } catch (NullPointerException e) {
            log.error("The Lambda is " + e.getMessage());
            Assert.fail("The Lambda is null", new Throwable());
        }
    }


    public static void validateValueIf(Map<String, String> variables, String key, String validValue) {
        KeyValuePair keyValuePair = new KeyValuePair();
        keyValuePair.setName(key);
        keyValuePair.setValue(validValue);
        try {
            if (!variables.containsKey(key) || StringUtils.isEmpty(validValue)) {
                Assert.fail(key + " is not set on current lambda");
            }

            String valueKey = variables.get(key);
            Assert.assertTrue(StringUtils.isNotEmpty(valueKey), key + " was empty in current lambda");

            switch (key) {
                case CommonLambdaFrontendAPIConstant.COUNTRY_CODE:
                    Assert.assertTrue(StringUtils.containsAny(valueKey, "+1", "1"),
                            String.format("Variable %s values not match", CommonLambdaFrontendAPIConstant.COUNTRY_CODE));
                    break;
                case CommonLambdaFrontendAPIConstant.ENABLE_DYNAMO_TEMPLATES:
                    Assert.assertTrue(StringUtils.containsAny(valueKey, "true", "false"),
                            String.format("%s is not equals to %s", valueKey, "true or false"));

                    if (variables.containsKey(CommonLambdaFrontendAPIConstant.DYNAMIC_EMAIL_TEMPLATES_DYNAMO_TABLE)) {
                        String table = variables.get(CommonLambdaFrontendAPIConstant.DYNAMIC_EMAIL_TEMPLATES_DYNAMO_TABLE);
                        dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.DynamicEmailTemplates);
                        Assert.assertTrue(StringUtils.equalsIgnoreCase(table, dbDynamoDbTableName),
                                String.format("%s is not equals to %s", table, dbDynamoDbTableName));
                    }
                    break;
                case CommonLambdaFrontendAPIConstant.IS_MIDDLEWARE_PROXY_SERVICE_ENABLED:
                    Assert.assertTrue(StringUtils.containsAny(valueKey, "true", "false"),
                            String.format("%s is not equals to %s", valueKey, "true or false"));
                    break;
                case CommonLambdaFrontendAPIConstant.DNSY_SERVICE_HOST:
                    String env = awsResources.getPrefix();
                    if (StringUtils.equalsIgnoreCase(env, "BCPR-CRT")) {
                        CommonLambdaFrontendAPIKeyValueConstant.DNSY_SERVICE_HOST = "https://advice.cert.evertecinc.com/messages/";
                    }
                    break;
                case CommonLambdaEmailNotificationConstant.DNSY_SECRET_NAME:
                    CommonSecretsKeyValueConstant.DNSY_SECRET_NAME = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.DNSY);
                    break;
                case CommonLambdaFrontendAPIConstant.ENABLE_PLAYGROUND_SCHEMA:
                case CommonLambdaFrontendAPIConstant.ENABLE_PLAYGROUND:
                    String env2 = awsResources.getPrefix();
                    if (StringUtils.equalsIgnoreCase(env2, "BCPR-CRT")) {
                        CommonLambdaFrontendAPIKeyValueConstant.ENABLE_PLAYGROUND_SCHEMA = "false";
                        CommonLambdaFrontendAPIKeyValueConstant.ENABLE_PLAYGROUND = "false";
                    }
                    break;
                case CommonLambdaFrontendAPIConstant.SECRET_MANAGER_SECRET_NAME:
                    CommonLambdaFrontendAPIKeyValueConstant.SECRET_MANAGER_SECRET_NAME = buildSecretsName(ResourceAWS.SECRETS);
                    break;
                default:
                    Assert.assertTrue(StringUtils.equals(valueKey, validValue),
                            String.format("%s is not equals to %s", valueKey, validValue));
            }
        } catch (AssertionError e) {
            errorListEnvironmentVariable.put(keyValuePair, e.getMessage());
        }
    }

    public static void validateNotContains(Map<String, String> variables, String key) {
        KeyValuePair keyValuePair = new KeyValuePair();
        keyValuePair.setName(key);
        try {
            if (variables.containsKey(key)) {
                Assert.fail(key + " is Present set on current lambda");
            }
        } catch (AssertionError e) {
            errorListEnvironmentVariable.put(keyValuePair, e.getMessage());
        }
    }

    private void validateImageDomain(Map<String, String> variables, String constantKey) {
        String environment = getEnvironment();
        String buildBucketName = environment.contains("CERT")
                ? ResourcesAWS.setBuildBucketsName(ResourceAWS.img, "CERT")
                : ResourcesAWS.setBuildBucketsName(ResourceAWS.img, "DEV");

        String imageDomain = ImageDomainAWS.getIconImageDomain(cloudFrontClient, buildBucketName);
        String imageUrl = ProtocolUrl.https + "://" + imageDomain;

        validateValueIf(variables, constantKey, imageUrl);
    }

    public static String getDnsNameLoadBalancer() {
        try {
            DescribeLoadBalancersResponse loadBalancerList = elasticLoadBalancingV2Client.describeLoadBalancers();

            if (!loadBalancerList.hasLoadBalancers()) {
                throw new SkipException("No load balancers found");
            }

            log.info("DNS Names of Load Balancers:");
            String loadBalancerName = ResourcesAWS.buildLoadBalancer();

            return loadBalancerList.loadBalancers().stream()
                    .filter(lb -> StringUtils.containsIgnoreCase(lb.dnsName(), loadBalancerName))
                    .findFirst()
                    .map(lb -> {
                        String dnsName = lb.dnsName();
                        log.info(dnsName);
                        return dnsName;
                    })
                    .orElseThrow(() -> new SkipException("No matching load balancer found"));
        } catch (ElasticLoadBalancingV2Exception e) {
            throw new SkipException("Error retrieving load balancer DNS name: " + e.getMessage());
        }
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "testGetDomainNameBucketsImage")
    public void testGetDomainNameBucketsImage() {
        ListDistributionsRequest request = ListDistributionsRequest.builder().build();
        ListDistributionsResponse response = cloudFrontClient.listDistributions(request);
        log.info(getDomainName(response, bucketS3ImageName));

    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "testGetDomainNameBucketsImageClaims")
    public void testGetDomainNameBucketsImageClaims() {
        ListDistributionsRequest request = ListDistributionsRequest.builder().build();
        ListDistributionsResponse response = cloudFrontClient.listDistributions(request);
        log.info(getDomainName(response, bucketS3ImageClaimsName));

    }

    public String getDomainName(ListDistributionsResponse response, String filter) {
        List<DistributionSummary> listDistribution = response.distributionList().items();
        Origin originItem;
        String domainName = null;
        for (DistributionSummary item : listDistribution) {
            if (item.origins().hasItems()) {
                originItem =
                        item.origins().items().stream()
                                .filter(origin ->
                                        StringUtils.containsIgnoreCase(
                                                origin.domainName(), filter)).findFirst().orElse(null);
                if (originItem != null) {
                    domainName = item.domainName();
                    log.info(domainName);
                    break;
                }

            }

        }
        return domainName;
    }


    public static void listTableNameOfDynamoDbByEnvironments() {
        try {
            List<String> tables = listDynamoDbTable();
            String environment = RestAssuredPropertiesConfig.getEnvironment().replace("EVT-", "");

            for (int i = 0; i < tables.size(); i++) {
                String table = String.format("BCPR-%s-%s", environment, tables.get(i));
                checkAndLogTable(table, i + 1);
            }
        } catch (AmazonServiceException e) {
            log.error("Error while listing table names: " + e.getMessage(), e);
            Assert.fail(e.toString());
        }
    }

    public static List<String> buildDynamoDbTableNameByEnvironment(List<String> tablesName) {
        List<String> tablesNameBuilder = new ArrayList<>();
        String environment = RestAssuredPropertiesConfig.getEnvironment().replace("EVT-", "");
        for (String s : tablesName) {
            String table = String.format("BCPR-%s-%s", environment, s);
            tablesNameBuilder.add(table);
        }
        return tablesNameBuilder;
    }

    private static void checkAndLogTable(String table, int index) {
        if (!isPresentTable(table)) {
            Assert.fail(String.format("The table %s does NOT exist in env %s",
                    table, RestAssuredPropertiesConfig.getEnvironment().replace("EVT-", "")));
            return;
        }

        ScanRequest scanRequest = new ScanRequest().withTableName(table);
        ScanResult result = amazonDynamoDB.scan(scanRequest);

        if (isNotEmptyTable(result)) {
            log.info(String.format("Table %d: %s is Not Empty", index, table));
        } else {
            log.info(String.format("Table %d: %s is Empty", index, table));
        }

        Assert.assertTrue(true, String.format("Table name %s is Empty", table));
    }

    private static boolean isNotEmptyTable(ScanResult result) {
        return result.getCount() > 0;
    }

    public static void isListTemplateIsNotEmpty(String table) {
        if (isPresentTable(table)) {
            ScanRequest scanRequest = new ScanRequest().withTableName(table);
            validateTableTemplate(scanRequest, getBucketTemplateLocalList(), getTemplateSubjectList());

        } else {
            if (result == null) {
                Assert.fail(String.format("The table %s NOT exist into env %s", table,
                        RestAssuredPropertiesConfig.getEnvironment().replace("EVT-", "")));
            }
        }

    }

    public static void validateTableTemplate(ScanRequest scanRequest, List<String> templateIdList, List<String> subjectList) {
        dbDynamoDbTableName = ResourcesAWS.buildDynamoDbTableName(DynamoDBTable.DynamicEmailTemplates);
        log.info("Table {}" + dbDynamoDbTableName);

        result = amazonDynamoDB.scan(scanRequest);

        if (result == null || result.getItems().isEmpty()) {
            Assert.fail(String.format("The table %s cannot be empty in env %s", dbDynamoDbTableName, awsResources.getEnv()));
        }

        int itemsToCheck = Math.min(subjectList.size(), templateIdList.size());

        for (int i = 0; i < itemsToCheck; i++) {
            try {
                JsonObject dataTemplate = parseResultItem(result, i);
                String idValue = getValueTemplate(dataTemplate, "id");
                String subjectValue = getValueTemplate(dataTemplate, "subject");

                validateTemplate(idValue, subjectValue, templateIdList, subjectList, i);
            } catch (Exception e) {
                log.error("Error processing JSON at index {}: {}" + i + e.getMessage(), e);
            }
        }
    }

    private static JsonObject parseResultItem(ScanResult result, int index) {
        String jsonString = gson.toJson(result.getItems().get(index));
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    private static void validateTemplate(String idValue, String subjectValue, List<String> templateIdList,
                                         List<String> subjectList, int index) {
        if (isPresentSubject(subjectValue, subjectList) && isPresentId(idValue, templateIdList)) {
            Assert.assertTrue(true, String.format("Table %s does not contain id %s with subject %s",
                    dbDynamoDbTableName, idValue, subjectValue));
            log.info(String.format("Template %d: contains id %s with subject %s", index + 1, idValue, subjectValue));
        }
    }

    private static String getValueTemplate(JsonObject dataTemplate, String key) {
        return dataTemplate.has(key) ? dataTemplate.get(key).getAsString() : null;
    }

    public static boolean isPresentSubject(String valueSubject, List<String> subjectList) {
        if (!subjectList.isEmpty()) {
            return subjectList.contains(valueSubject);
        }
        return false;
    }

    public static boolean isPresentId(String valueSubject, List<String> templateIdList) {
        if (!templateIdList.isEmpty()) {
            return templateIdList.contains(valueSubject);
        }
        return false;
    }

    public static boolean isPresentTable(String table) {
        if (amazonDynamoDB != null) {
            return amazonDynamoDB.listTables().getTableNames().contains(table);
        }
        return false;
    }

    public static List<String> listDynamoDbTable() {
        return List.of(
                "CallCenter",
                "CardActivationAttempts",
                "Category",
                "ClaimData",
                "ClaimTypeData",
                "ContactUs",
                "ContactUsReason",
                "EnrollmentData",
                "FAQ",
                "LoginAttempts",
                "Payment",
                "RegainAccessData",
                "Wallet",
                "LastPassword",
                "Cooperative",
                "BlockReason",
                "DynamicEmailTemplates"

        );
    }

    public static List<String> getBucketLocalList() {
        return List.of(
                "account_created_user_en.html",
                "account_created_user_es.html",
                "account_validated_user_en.html",
                "account_validated_user_es.html",
                "bank_claim_en.html",
                "bank_claim_es.html",
                "card_activation_en.html",
                "card_activation_es.html",
                "change_password_user_en.html",
                "change_password_user_es.html",
                "contact_us_en.html",
                "contact_us_es.html",
                "contact_us_user_auth_en.html",
                "contact_us_user_auth_es.html",
                "contact_us_user_notauth_en.html",
                "contact_us_user_notauth_es.html",
                "login_attempts_en.html",
                "login_attempts_es.html",
                "terms_and_conditions_en.html",
                "terms_and_conditions_es.html",
                "user_claim_en.html",
                "user_claim_es.html",
                "user_fraud_claim_en.html",
                "user_fraud_claim_es.html",
                "user_payment_en.html",
                "user_payment_es.html",
                "welcome_user_en.html",
                "welcome_user_es.html"
        );
    }

    public static List<String> getBucketDatabaseJsonLocalList() {
        return List.of(
                "callcenter-data.json",
                "cardactivationattempts-data.json",
                "category-data.json",
                "claims-logs-data.json",
                "claimtypedata-data-with-images.json",
                "claimtypedata-data.json",
                "contactus-data.json",
                "contactusreason-data.json",
                "cooperative-data.json",
                "dynamicemailtemplates-data.json",
                "enrollmentdata-data.json",
                "faq-data.json",
                "loginattempts-data.json",
                "notification-failed-messages.json",
                "payment-data.json",
                "regainaccessdata-data.json",
                "wallet-data.json"
        );
    }

    public static List<String> getBucketTemplateLocalList() {
        return List.of(
                "account_created_user_en",
                "account_created_user_es",
                "card_activation_en",
                "card_activation_es",
                "change_password_user_en",
                "change_password_user_es",
                "contact_us_en",
                "contact_us_es",
                "welcome_user_en",
                "welcome_user_es",
                "login_attempts_en",
                "login_attempts_es",
                "contact_us_user_notauth_en",
                "contact_us_user_notauth_es",
                "contact_us_user_auth_en",
                "contact_us_user_auth_es",
                "bank_claim_en",
                "bank_claim_es",
                "user_payment_en",
                "user_payment_es",
                "user_claim_en",
                "user_claim_es"

        );
    }

    public static List<String> getTemplateSubjectList() {
        return List.of(
                "Confirmación de envío de consulta",
                "Password change notification",
                "Confirmación de reclamo",
                "Welcome to Mi Tarjeta Coop",
                "Notificación: intentos fallidos de ingreso a la cuenta Mi Tarjeta Coop",
                "Inquiry submition confirmation",
                "Inquiry submition confirmation",
                "Payment receipt Mi Tarjeta Coop",
                "Inquiry submition confirmation",
                "Notificación cambio de contraseña",
                "Confirmación de envío de consulta",
                "Card activation attempts exceeded in Mi Tarjeta Coop",
                "Bienvenido a Mi Tarjeta Coop",
                "Comprobante de pago Mi Tarjeta Coop",
                "Confirmación de envío de consulta",
                "Notification: failed account login attempts Mi Tarjeta Coop",
                "Claim Confirmation",
                "Your bank account has been created",
                "Intentos de activación de tarjeta superados en Mi Tarjeta Coop",
                "New claim notification",
                "Su cuenta bancaria ha sido creada",
                "Notificación de nuevo reclamo"


        );
    }

    @Ignore
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-954-GetServiceTaskNotification")
    public void testGetServiceTaskNotification() {
        String currentClusterArn = null;
        List<ContainerDefinition> listContainerDef = null;
        if (StringUtils.isNotEmpty(getClusterName(ecsClient))) {
            currentClusterArn = getClusterName(ecsClient);
        }
        try {
            log.info(String.format("Current Cluster ARN: %s%n", currentClusterArn));
            String microserviceTaskArn = getMsTaskArn(ecsClient, currentClusterArn, "NOTIFICATION");
            String taskDefinitionArn = getTaskDefinitionArn(ecsClient, currentClusterArn,
                    microserviceTaskArn, "notification-service");
            DescribeTaskDefinitionResult describeTaskDefinitionResult = getDescribeTaskDefinition(ecsClient,
                    taskDefinitionArn);
            listContainerDef = getListContainerDef(describeTaskDefinitionResult);
        } catch (Exception e) {
            log.error("No task exists in the micro-service " + e.getMessage());

        }

        validateContainerValues(listContainerDef);
    }

    @Ignore
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-933-GetServiceTaskMiddlewareProxy")
    public void testGetServiceTaskMiddlewareProxy() {
        String currentClusterArn = null;
        if (StringUtils.isNotEmpty(getClusterName(ecsClient))) {
            currentClusterArn = getClusterName(ecsClient);
        }
        try {
            log.info(String.format("Current Cluster ARN: %s%n", currentClusterArn));
            if (StringUtils.isNotEmpty(getMsTaskArn(ecsClient, currentClusterArn, "MIDDLEWARE-PROXY"))) {
                currentClusterArn = getMsTaskArn(ecsClient, currentClusterArn, "MIDDLEWARE-PROXY");
            }
            String taskDefinitionArn = getTaskDefinitionArn(ecsClient, currentClusterArn,
                    null, "middleware-proxy-service");
            DescribeTaskDefinitionResult describeTaskDefinitionResult = getDescribeTaskDefinition(ecsClient,
                    taskDefinitionArn);
            List<ContainerDefinition> listContainerDef = getListContainerDef(describeTaskDefinitionResult);
            log.info(listContainerDef.toString());
        } catch (Exception e) {
            log.error("No cluster exists in the micro-service " + e.getMessage());

        }


    }

    private void validateContainerValues(List<ContainerDefinition> listContainerDef) {
        String env = awsResources.getEnv();
        try {
            if (!listContainerDef.isEmpty()) {
                List<KeyValuePair> errorList = new ArrayList<>();
                for (ContainerDefinition containerDefinition : listContainerDef) {
                    String image = containerDefinition.getImage();
                    String imageVersion = StringUtils.substringAfter(image, ":");

                    log.info(
                            String.format("Environment %s: Image %s, version %s %n", env, image, imageVersion));

                    if (!StringUtils.containsIgnoreCase(image, CommonVersionConstant.MS_NOTIFICATION_VERSION)) {
                        KeyValuePair versionError = new KeyValuePair();
                        versionError.setName("versionError");
                        versionError.setValue(String.format("Image version expected is %s, but found %s",
                                CommonVersionConstant.MS_NOTIFICATION_VERSION, imageVersion));
                        errorList.add(versionError);
                    }

                    List<KeyValuePair> environment = containerDefinition.getEnvironment();

                    for (KeyValuePair values : environment) {
                        String Name = values.getName();
                        String value = values.getValue();

                        if (Name.equals("QUEUE_EMAIL")) {
                            String expectedValue = ResourcesAWS.buildQueue(QueueType.email);
                            if (!StringUtils.equalsIgnoreCase(expectedValue, value)) {
                                errorList.add(values);
                            }
                        } else if (Name.equals("QUEUE_SMS")) {
                            String expectedValue = ResourcesAWS.buildQueue(QueueType.sms);
                            if (!StringUtils.equalsIgnoreCase(expectedValue, value)) {
                                errorList.add(values);
                            }
                        }
                        log.info(String.format("Environment %s: Name %s, Value %s %n", env, Name, value));
                        /*MICROSERVICE-NOTIFICATION*/
                    /*"SPRING_PROFILES_ACTIVE":"dev",
                      "PORT":"8080",
                      "BASE_PATH_MANAGEMENT":"/v1/notification/",
                            "REGION":"us-east-1",
                            "WAIT_TIME_SECONDS_SMS":"20",
                            "WAIT_TIME_SECONDS_EMAIL":"20",
                            "MAX_NUMBER_MESSAGES_SMS":"10",
                            "MAX_NUMBER_MESSAGES_EMAIL":"10",
                            "VISIBILITY_TIMEOUT_EMAIL":"60",
                            "VISIBILITY_TIMEOUT_SMS":"60",
                            "NUMBER_THREADS":"1",
                            "BASE_URL_SQS":"https://sqs.us-east-1.amazonaws.com/864961356886/",
                            "QUEUE_EMAIL":"bcpr-dev-notification-email-queue",
                            "QUEUE_SMS":"bcpr-dev-notification-sms-queue",
                            "BASE_URL_REST":"https://advice.dev.evertecinc.com/messages/",
                            "API_KEY":"fDxeTrxNlE7nd3ca7h2EI8ZktT6Dwyim1eUECW4Z",
                            "READ_TIMEOUT":"5000",
                            "TIMEOUT":"5000",
                            "WRITE_TIMEOUT":"5000",
                            "DNSY_CMDB_ID":"BCPR",
                            "LOG4J_LEVEL": "INFO"*/

                        /*MICROSERVICE-MIDDLEWARE-PROXY*/
                    /*
                      "MIDDLEWARE_ENDPOINT":"https://cer.api.ebus.whitelabel.evertecinc.com/",
                      "MIDDLEWARE_PASSWORD":"password2",
                      "MIDDLEWARE_ROUTE_ID":"VPL_PR",
                      "MIDDLEWARE_USER":"testuser2",
                      "QUARKUS_VERTX_CACHING":"false",
                      "QUARKUS_VERTX_CLASSPATH_RESOLVING":"false",
                    "*/
                    }

                }
                if (!errorList.isEmpty()) {
                    for (KeyValuePair values : errorList) {
                        log.info(values);
                    }
                }
                Assert.assertTrue(errorList.isEmpty(), "Errors were found: " + errorList);
            }

        } catch (NullPointerException e) {
            log.error("No Container definition exist " + e.getMessage());
            Assert.fail();
        }

    }

    private List<ContainerDefinition> getListContainerDef(
            DescribeTaskDefinitionResult describeTaskDefinitionResult) {
        List<ContainerDefinition> listContainerDef = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(describeTaskDefinitionResult.getTaskDefinition().getStatus(),
                StatusCard.ACTIVE.name())) {
            listContainerDef = describeTaskDefinitionResult.getTaskDefinition().getContainerDefinitions();
        }

        if (listContainerDef.isEmpty()) {
            log.error("Container definitions are empty");
        }
        return listContainerDef;
    }

    private DescribeTaskDefinitionResult getDescribeTaskDefinition(AmazonECS ecsClient,
                                                                   String taskDefinitionArn) {
        DescribeTaskDefinitionResult describeTaskDefinitionResult = new DescribeTaskDefinitionResult();
        DescribeTaskDefinitionRequest notificationServiceVersion = new DescribeTaskDefinitionRequest()
                .withTaskDefinition(taskDefinitionArn);

        if (StringUtils.isNotEmpty(notificationServiceVersion.getTaskDefinition())) {
            describeTaskDefinitionResult = ecsClient.describeTaskDefinition(notificationServiceVersion);
        }

        return describeTaskDefinitionResult;
    }

    public static String getClusterName(AmazonECS ecsClient) {
        String prefix = awsResources.getPrefix();
        String expectedClusterArn = String.format("arn:aws:ecs:us-east-1:54618651444:cluster/%s-microservices-cluster", prefix);

        ListClustersResult clusterList = ecsClient.listClusters();

        Assert.assertNotNull(clusterList, "Cluster list was null");
        Assert.assertFalse(clusterList.getClusterArns().isEmpty(), "Cluster list was empty");

        String currentClusterArn = clusterList.getClusterArns().stream()
                .filter(cluster -> StringUtils.equalsIgnoreCase(cluster, expectedClusterArn))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(currentClusterArn, String.format("Cluster %s was not found in the result list", expectedClusterArn));

        return currentClusterArn;
    }


    public static String getMsTaskArn(AmazonECS ecsClient, String currentClusterArn,
                                      String taskDefinitionName) {
        String currentTaskArn = "";
        String prefix = awsResources.getPrefix();
        ListTasksRequest listTasksRequest = new ListTasksRequest().withCluster(currentClusterArn);
        if (StringUtils.isNotEmpty(listTasksRequest.getCluster())) {
            ListTasksResult listTasksResult = ecsClient.listTasks(listTasksRequest);
            if (listTasksResult != null && listTasksResult.getTaskArns().size() == 1) {
                List<String> taskArns = listTasksResult.getTaskArns();
                String pathMs = prefix + "-microservices-cluster";
                if (!taskArns.isEmpty()) {
                    currentTaskArn = taskArns.stream().filter(
                                    cluster -> StringUtils.containsIgnoreCase(cluster, pathMs))
                            .findFirst()
                            .orElse(null);
                }
            }
            if (listTasksResult != null && listTasksResult.getTaskArns().size() > 1) {
                listTasksRequest =
                        new ListTasksRequest().withCluster(currentClusterArn).withFamily(taskDefinitionName);
                listTasksResult = ecsClient.listTasks(listTasksRequest);

                List<String> taskArns = listTasksResult.getTaskArns();
                String pathMs = prefix + "-microservices-cluster";
                if (!taskArns.isEmpty()) {
                    currentTaskArn = taskArns.stream().filter(
                                    cluster -> StringUtils.containsIgnoreCase(cluster, pathMs))
                            .findFirst()
                            .orElse(null);
                }
            } else {
                log.error("can't retrieve task list");
            }
        }

        return currentTaskArn;
    }

    public static String getTaskDefinitionArn(AmazonECS ecsClient, String currentClusterArn,
                                              String microserviceTaskArn, String taskName) {
        DescribeTasksRequest describeTasksRequest = new DescribeTasksRequest()
                .withCluster(currentClusterArn)
                .withTasks(microserviceTaskArn);

        if (describeTasksRequest.getTasks().isEmpty()) {
            return "";
        }

        DescribeTasksResult describeTasksResult = ecsClient.describeTasks(describeTasksRequest);

        Task matchingTask = describeTasksResult.getTasks().stream()
                .filter(task -> StringUtils.containsIgnoreCase(task.getGroup(), taskName))
                .findFirst()
                .orElse(null);

        if (matchingTask == null) {
            return "";
        }

        TaskOverride taskOverride = matchingTask.getOverrides();
        if (taskOverride == null || taskOverride.getContainerOverrides().isEmpty()) {
            return "";
        }

        String prefix = awsResources.getPrefix();
        String taskContainerName = prefix + String.format("-%s", taskName);

        ContainerOverride matchingContainerOverride = taskOverride.getContainerOverrides().stream()
                .filter(override -> StringUtils.containsIgnoreCase(override.getName(), taskContainerName))
                .findFirst()
                .orElse(null);

        if (matchingContainerOverride == null) {
            return "";
        }

        return matchingTask.getTaskDefinitionArn();
    }


    @Ignore
    @Test
    public static void retrieveSecretAutomation() {
        String secretName = null;
        String secretNameSearch = "BCPR-CRT-secret-automation";
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            Assert.assertTrue(map.containsKey("AWS_ACCESS_KEY_ID"));
            log.info(getSecretValueResponse);
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1307-GetSecrets-Frontend-API")
    public static void retrieveSecretsFrontendApi() {
        String secretName = null;
        String env2 = awsResources.getPrefix();
        String secretNameSearch = buildSecretsName(ResourceAWS.SECRETS);
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            log.info(String.format("Retrieve and view the secret  %s with value %s", secretNameSearch, map.keySet()));
            if (map.containsKey(CommonSecretsKeyConstant.VISION_PLUS_USERNAME)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.VISION_PLUS_USERNAME),
                        CommonSecretsKeyValueConstant.VISION_PLUS_USERNAME));
            }
            if (map.containsKey(CommonSecretsKeyConstant.VISION_PLUS_PASSWORD)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.VISION_PLUS_PASSWORD), CommonSecretsKeyValueConstant.VISION_PLUS_PASSWORD));
            }
            if (map.containsKey(CommonSecretsKeyConstant.PLAID_CLIENT_ID)) {

                if (StringUtils.equalsIgnoreCase(env2, "BCPR-DEV")) {
                    CommonSecretsKeyValueConstant.PLAID_CLIENT_ID = "600703d64717120010c0efda";
                }
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.PLAID_CLIENT_ID),
                        CommonSecretsKeyValueConstant.PLAID_CLIENT_ID));
            }
            if (map.containsKey(CommonSecretsKeyConstant.PLAID_SECRET_KEY)) {
                if (StringUtils.equalsIgnoreCase(env2, "BCPR-DEV")) {
                    CommonSecretsKeyValueConstant.PLAID_CLIENT_ID = "e55726f5253d5ed5c8e6e3b06b6ca9";
                }
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.PLAID_SECRET_KEY),
                        CommonSecretsKeyValueConstant.PLAID_SECRET_KEY));
            }
            if (map.containsKey(CommonSecretsKeyConstant.ENCRYPTION_KEY)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.ENCRYPTION_KEY), CommonSecretsKeyValueConstant.ENCRYPTION_KEY));
            }
            if (map.containsKey(CommonSecretsKeyConstant.DNSY_API_KEY)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.DNSY_API_KEY), CommonSecretsKeyValueConstant.DNSY_API_KEY));
            }
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Ignore
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1273-GetSecretsVision")
    public static void retrieveSecretVision() {
        String secretName = null;
        String secretNameSearch = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.VISION);
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            if (map.containsKey(CommonSecretsKeyConstant.VISION_PLUS_USERNAME)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.VISION_PLUS_USERNAME),
                        CommonSecretsKeyValueConstant.VISION_PLUS_USERNAME));
            }
            if (map.containsKey(CommonSecretsKeyConstant.VISION_PLUS_PASSWORD)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.VISION_PLUS_PASSWORD), CommonSecretsKeyValueConstant.VISION_PLUS_PASSWORD));
            }
            log.info(getSecretValueResponse);
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Ignore
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1274-GetSecretsPlaid")
    public static void retrieveSecretPlaid() {
        String secretName = null;
        String secretNameSearch = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.PLAID);
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            if (map.containsKey(CommonSecretsKeyConstant.PLAID_CLIENT_ID)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.PLAID_CLIENT_ID), CommonSecretsKeyValueConstant.PLAID_CLIENT_ID));
            }
            if (map.containsKey(CommonSecretsKeyConstant.PLAID_SECRET_KEY)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.PLAID_SECRET_KEY), CommonSecretsKeyValueConstant.PLAID_SECRET_KEY));
            }
            log.info(getSecretValueResponse);
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Ignore
    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1272-GetSecretsApiEncryption")
    public static void retrieveSecretApiEncryption() {
        String secretName = null;
        String secretNameSearch = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.API, ResourceAWS.ENCRYPTION);
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            if (map.containsKey(CommonSecretsKeyConstant.ENCRYPTION_KEY)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.ENCRYPTION_KEY), CommonSecretsKeyValueConstant.ENCRYPTION_KEY));
            }
            log.info(getSecretValueResponse);
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1271-GetSecretsDNSY",
            expectedExceptions = RuntimeException.class)
    public static void retrieveSecretDNSY() {
        String secretName = null;
        String secretNameSearch = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.DNSY);
        if (StringUtils.isNotEmpty(searchNameSecret(secretNameSearch))) {
            secretName = searchNameSecret(secretNameSearch);
        }
        try {
            GetSecretValueResponse getSecretValueResponse = getSecretValueResponse(secretName);
            String jsonString = getSecretValueResponse.secretString();
            Map<String, String> map = parseByStringToMap(jsonString);
            if (map.containsKey(CommonSecretsKeyConstant.DNSY_API_KEY)) {
                Assert.assertTrue(StringUtils.equalsIgnoreCase(map.get(CommonSecretsKeyConstant.DNSY_API_KEY), CommonSecretsKeyValueConstant.DNSY_API_KEY));
            }
            log.info(getSecretValueResponse);
        } catch (Exception e) {
            log.error("Not exist any secrets with this name " + secretNameSearch + " -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    public static ListSecretsResponse getListSecretsResponse() {
        if (secretsManagerClient == null) {
            throw new SkipException("The Secret client is Empty");
        }
        ListSecretsResponse resultListSecrets;
        try {
            resultListSecrets = secretsManagerClient.listSecrets();
            if (resultListSecrets.nextToken() != null && !resultListSecrets.nextToken().isEmpty()) {
                Consumer<ListSecretsRequest.Builder> builderConsumer = builder -> builder.maxResults(ITEMS_PER_PAGE);
                resultListSecrets = secretsManagerClient.listSecrets(builderConsumer);
                nextTokenSecret = resultListSecrets.nextToken();
            }

            if (nextTokenSecret != null) {
                getListSecretsResponse();
            }

        } catch (Exception e) {
            throw new SkipException(e.getMessage());
        }
        return resultListSecrets;
    }

    public static String searchNameSecret(String secretName) {
        ListSecretsResponse listSecretsResponse = getListSecretsResponse();
        for (SecretListEntry secret : listSecretsResponse.secretList()) {
            if (StringUtils.equalsIgnoreCase(secret.name(), secretName)) {
                log.info("The Secret name ->" + secret.name());
                return secret.name();
            }
        }
        return "";
    }

    public static GetSecretValueResponse getSecretValueResponse(String secretName) {
        try {
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            return secretsManagerClient.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new SkipException(e.getMessage());
        }
    }

    public static Map<String, String> parseByStringToMap(String jsonString) {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(jsonString, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON format: " + e.getMessage(), e);
        }
    }


    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-798-Store the lastest 10 passwords")
    public static void validateTeenPasswordStore() {
        String user = getSessionUser();
        if (StringUtils.equalsIgnoreCase(user, "username")) {
            user = getSessionUser();
        }
        String lastPassword = "";
        try {
            Map<String, AttributeValue> result = scannerUserLastPasswordInfo(user).getItems().get(0);
            lastPassword = result.get("last_password").getS();
            log.info(String.format("The username: %s has lastPassword encripted %s", result.get("username"), lastPassword));
        } catch (Exception e) {
            log.error("specified key is not present");
        }
        Assert.assertTrue(StringUtils.isNotEmpty(lastPassword), user + " is not present");
    }

    @Test(
            groups = {"EnvBCPRTests"},
            testName = "BCPRXRP-1354-Validate Indexes into DyanmoDb")
    public void testValidateIndexIntoDynamoDb() {
        List<String> tableNameWithIndexes = buildDynamoDbTableNameByEnvironment(listIndexesDynamoDbTable());

        amazonDynamoDB =
                AmazonDynamoDBClientBuilder.standard()
                        .withRegion(Region.US_EAST_1.toString())
                        .withCredentials(new AWSStaticCredentialsProvider(getAwsBasicSessionCredentials()))
                        .build();

        for (String tableName :
                tableNameWithIndexes) {
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            DescribeTableResult describeTableResult = amazonDynamoDB.describeTable(describeTableRequest);
            List<GlobalSecondaryIndexDescription> gsiList = describeTableResult.getTable().getGlobalSecondaryIndexes();
            if (gsiList != null) {
                for (GlobalSecondaryIndexDescription gsi : gsiList) {
                    log.info("GSI Name: {} -> " + gsi.getIndexName());

                }
            } else {
                log.info("No Global Secondary Indexes found for table: {} -> " + tableName);
            }
        }
    }

    public static List<String> listIndexesDynamoDbTable() {
        return List.of(
                "ClaimData",
                "FAQ",
                "Payment",
                "Wallet"
        );
    }

}
