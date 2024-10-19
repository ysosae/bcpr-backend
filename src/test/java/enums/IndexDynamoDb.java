package enums;

public enum IndexDynamoDb {
  INDEX_DYNAMO_DB_CLAIM_ID("id-index"),
  INDEX_DYNAMO_DB_CLAIM_SUB_ID("subId-index"),
  INDEX_DYNAMO_DB_PAYMENT_CREATE_ON("subId-createdOn-index"),
  INDEX_DYNAMO_DB_PAYMENT_STATUS("status-createdOn-index"),
  INDEX_DYNAMO_DB_FAQs("categoryId-index"),
  INDEX_DYNAMO_DB_WALLET_CREATE_ON("subId-createdOn-index"),
  INDEX_DYNAMO_DB_WALLET_ACCOUNT_ID("subId-accountId-index");

  public final String message;

  public String getMessage() {
    return message;
  }

  IndexDynamoDb(String message) {
    this.message = message;
  }
}
