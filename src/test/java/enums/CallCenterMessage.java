package enums;

public enum CallCenterMessage {
  CALL_CENTER_SCHEDULE_MEMBER_SERVICE_CENTER_EN("Member service available 24/7 all year, only for Transactions related to your credit card VISA or Mastercard of the Credit Union."),
  CALL_CENTER_SCHEDULE_MEMBER_SERVICE_CENTER_ES("Servicio al cliente disponible 24/7 todo el año, solo para transacciones relacionadas a su tarjeta de crédito VISA o Mastercard de la Cooperativa."),
  CALL_CENTER_SCHEDULE_AUTOMATED_SERVICE_EN("Automated system available 24/7 all year, you can consult the last 10 transactions, activate your account, review balance and obtain the pin of your credit card."),
  CALL_CENTER_SCHEDULE_AUTOMATED_SERVICE_ES("Sistema automatizado disponible 24/7  todo el año, donde puedes consultar tus últimas 10 transacciones, activar cuenta, revisar balance y obtener pin de tu tarjeta de crédito."),
  CALL_CENTER_DESCRIPTION_MEMBER_SERVICE_CENTER_EN("Member Service Center"),
  CALL_CENTER_DESCRIPTION_MEMBER_SERVICE_CENTER_ES("Centro de Servicio al Socio"),
  CALL_CENTER_DESCRIPTION_AUTOMATED_SERVICE_EN("Credit Card Automated Service"),
  CALL_CENTER_DESCRIPTION_AUTOMATED_SERVICE_ES("Servicio Automatizado a Tarjetas de Crédito"),
  CALL_CENTER_PHONE_NUMBER_MEMBER_SERVICE_CENTER("787-641-2310"),
  CALL_CENTER_PHONE_NUMBER_AUTOMATED_SERVICE("787-281-8444");

  public final String message;

  public String getMessage() {
    return message;
  }

  CallCenterMessage(String message) {
    this.message = message;
  }
}
