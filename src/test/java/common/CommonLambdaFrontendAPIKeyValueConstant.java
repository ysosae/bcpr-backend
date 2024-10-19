package common;

import static config.ResourcesAWS.buildSecretsName;

import enums.ResourceAWS;

public class CommonLambdaFrontendAPIKeyValueConstant {

  public static final String DNSY_SMS_ENDPOINT = "https://advice.dev.evertecinc.com/messages/sms";
  public static final String DNSY_EMAIL_ENDPOINT = "https://advice.dev.evertecinc.com/messages/email";
  public static final String VISION_PLUS_ENDPOINT = "https://cer.api.ebus.whitelabel.evertecinc.com";
  public static final String ACH_SERVICE_HOST = "https://uat.mmpay.evertecinc.com/WebPaymentAPI/WebPaymentAPI.svc";
  public static String DNSY_SERVICE_HOST = "https://advice.dev.evertecinc.com/messages/";
  public static final String PLAID_SERVICE_HOST = "https://sandbox.plaid.com";
  public static final String ENROLLMENT_MAX_ATTEMPTS_CODE_RESENT = "3";
  public static final String DAYS_NEAR_TO_EXPIRE = "15";
  public static final String CODE_TIME_EXPIRATION_IN_MINUTES = "5";
  public static final String LOGGER_SERVICE_NAME = "bcpr-frontend-api";
  public static final String LOGGER_LEVEL = "debug";
  public static final String LOGGER_FORMAT = "json";
  public static final String LOGIN_MAX_ATTEMPTS_LIMIT = "3";
  public static final String PASSWORD_REPETITION_THRESHOLD = "10";
  public static final String API_VERSION_CDK = "2016-04-18";
  public static final String THRESHOLD_REWARDS_LIMIT = "5000";
  public static String ENABLE_PLAYGROUND = "true";
  public static String ENABLE_PLAYGROUND_SCHEMA = "true";
  public static final String CHANGE_PASSWORD_USER_HTML_TEMPLATE_NAME = "change_password_user";
  public static final String BANK_CLAIM_HTML_TEMPLATE_NAME = "bank_claim";
  public static final String SQS_API_VERSION = "2012-11-05";
  public static final String WELCOME_HTML_TEMPLATE_NAME = "welcome_user";
  public static final String MINUTES_TO_CALL_AUTH_MIDDLEWARE = "1438";
  public static final String USE_SQS_AS_NOTIFICATION_CHANNEL = "true";
  public static final String MAX_ALLOWED_INACTIVITY_TIME_SECONDS = "30";
  public static final String IDENTITY_POOL_ID = "us-east-1:86d8610d-726b-4642-8cf1-7b70abd21652";
  public static final String SQS_NOTIFICATION_SMS_QUEUE = "https://sqs.us-east-1.amazonaws.com/864961356886/bcpr-qa-mss-sms-queue";
  public static final String CONTACT_US_USER_AUTH_HTML_TEMPLATE_NAME = "contact_us_user_auth";
  public static final String AWSXRAY_ENABLED = "true";
  public static final String ENVIRONMENT = "qa";
  public static final String BLOCK_REASON_DYNAMO_TABLE = "BCPR-QA-BlockReason";
  public static final String VALID_USER_SESSION_TIME_SECONDS = "420";
  public static final String LOGIN_HTML_TEMPLATE_NAME = "login_attempts";
  public static final String CONTACT_US_HTML_TEMPLATE_NAME = "contact_us";
  public static final String URL_EXPIRATION_SECONDS = "300";
  public static final String MAX_RETRY_ATTEMPTS = "3";
  public static final String CARD_ACTIVATION_MAX_ATTEMPTS_LIMIT = "3";
  public static final String APP_LANGUAGES = "en,es";
  public static final String MAKE_PAYMENT_HTML_TEMPLATE_NAME = "user_payment";
  public static final String USER_CLAIM_HTML_TEMPLATE_NAME = "user_claim";
  public static final String SQS_NOTIFICATION_EMAIL_QUEUE = "https://sqs.us-east-1.amazonaws.com/864961356886/bcpr-qa-mss-email-queue";
  public static final String ACCOUNT_CREATED_HTML_TEMPLATE_NAME = "account_created_user";
  public static final String FAQ_REWARDS_CATEGORY = "4";
  public static  String SECRET_MANAGER_SECRET_NAME = buildSecretsName(ResourceAWS.SECRETS);
  }
