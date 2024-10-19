package common;

import static config.ResourcesAWS.buildSecretsName;

import enums.ResourceAWS;

public class CommonSecretsKeyValueConstant {

  public static final String DNSY_API_KEY = "fDxeTrxNlE7nd3ca7h2EI8ZktT6Dwyim1eUECW4Z";
  public static String DNSY_SECRET_NAME = buildSecretsName(ResourceAWS.SECRET, ResourceAWS.DNSY);
  public static final String VISION_PLUS_USERNAME = "testuser2";
  public static final String VISION_PLUS_PASSWORD = "password2";
  public static String PLAID_CLIENT_ID = "600703d64717120010c0efda";
  public static final String PLAID_SECRET_KEY = "e55726f5253d5ed5c8e6e3b06b6ca9";
  public static final String ENCRYPTION_KEY = "GL3J4734J834KJ5G728JKH";
  }
