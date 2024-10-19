package common;

import config.QueueAWS;

public class CommonLambdaKeyValueConstant {

  public static final String DNSY_API_KEY = "fDxeTrxNlE7nd3ca7h2EI8ZktT6Dwyim1eUECW4Z";
  public static final String DNSY_SMS_ENDPOINT = "https://advice.dev.evertecinc.com/messages/sms";
  public static final String DNSY_EMAIL_ENDPOINT = "https://advice.dev.evertecinc.com/messages/email";
  public static final String VISION_PLUS_ENDPOINT = "https://cer.api.ebus.whitelabel.evertecinc.com";
  public static final String ACH_SERVICE_HOST = "https://uat.mmpay.evertecinc.com/WebPaymentAPI/WebPaymentAPI.svc";
  public static final String DNSY_SERVICE_HOST = "https://advice.dev.evertecinc.com/messages/";
  public static final String PLAID_SERVICE_HOST = "https://sandbox.plaid.com";
  public static final String PLAID_CLIENT_ID = "630917a6bc22bd0014dea471";
  public static final String PLAID_SECRET_KEY = "e15bee55c593ea609bdc4c7211c5fa";
  public static final String VISION_PLUS_USERNAME = "testuser2";
  public static final String VISION_PLUS_PASSWORD = "password2";
  public static final String ENROLLMENT_MAX_ATTEMPTS_CODE_RESENT = "3";
  public static final String DAYS_NEAR_TO_EXPIRE = "15";
  public static final String CODE_TIME_EXPIRATION_IN_MINUTES = "5";
  public static final String LOGGER_SERVICE_NAME = "bcpr-frontend-api";
  public static final String LOGGER_LEVEL = "debug";
  public static final String LOGGER_FORMAT = "json";
  public static final String LOGIN_MAX_ATTEMPTS_LIMIT = "3";
  public static final String PASSWORD_REPETITION_THRESHOLD = "10";
  public static final String API_VERSION_CDK = "2016-04-18";
  public static final String THRESHOLD_REWARDS_LIMIT = "1000";
  public static final String ENABLE_PLAYGROUND = "true";
  public static final String ENABLE_PLAYGROUND_SCHEMA = "true";
  public static final String SQS_NOTIFICATION_EMAIL_QUEUE = QueueAWS.getUrlQueueEmail();
  public static final String SQS_NOTIFICATION_SMS_QUEUE = QueueAWS.getUrlQueueSms();
  public static final String USE_SQS_AS_NOTIFICATION_CHANNEL = "true";
  }
