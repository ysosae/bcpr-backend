package utils;


import static config.RestAssuredExtension.getOperationType;
import static config.RestAssuredExtension.setOperationType;
import static utils.AppDateFormats.getDateFormat;

import java.text.DecimalFormat;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class DataGenerator {
  private static final Logger log = Logger.getLogger(DataGenerator.class);

  public static String randomAmount() {
    Random rand = new Random();
    double number = rand.nextDouble() * 1000.0;
    DecimalFormat df = new DecimalFormat("$ #,###,##0.00");
    String formatted = df.format(number);
    log.info("Amount number: " + formatted);
    return formatted;
  }

  public static String randomUsername() {
    String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String numbers = "1234567890";
    String combinedChars = capitalCaseLetters + lowerCaseLetters + numbers;
    Random random = new Random();
    char[] username = new char[8];
    StringBuilder sb = new StringBuilder();

    username[0] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
    username[1] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
    username[2] = numbers.charAt(random.nextInt(numbers.length()));

    for (int i = 3; i < 8; i++) {
      username[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
    }

    for (char c : username) {
      sb.append(c);
    }
    log.info("USERNAME: " + sb);
    return sb.toString();
  }


  public static String randomNumber(int number) {
    Random rand = new Random();
    int n = rand.nextInt(number);
    n += 1;
    return String.valueOf(n);
  }

  public static String randomOperation() {
    String operationType = getOperationType();
    if (StringUtils.isNotEmpty(operationType)) {
      setOperationType("");
      return operationType;
    } else {
      final String[] type_noun = {"DEBIT", "CREDIT"};
      Random random = new Random();
      int index = random.nextInt(type_noun.length);
      return type_noun[index];
    }
  }

  public static String randomPassword(int length) {
    String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String specialCharacters = "!@#$%^&*()_+-=[]{};':|,.<>/?";
    String numbers = "1234567890";
    String combinedChars = capitalCaseLetters + lowerCaseLetters + numbers;
    Random random = new Random();
    char[] password = new char[length];
    StringBuilder sb = new StringBuilder();

    password[0] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
    password[1] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
    password[2] = numbers.charAt(random.nextInt(numbers.length()));
    password[3] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));


    for (int i = 4; i < length; i++) {
      password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
    }

    for (char c : password) {
      sb.append(c);
    }
    log.info("PASSWORD: " + sb);
    return sb.toString();
  }

  public static String randomUsername(int length) {
    String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String numbers = "1234567890";
    String combinedChars = capitalCaseLetters + lowerCaseLetters + numbers;
    Random random = new Random();
    char[] username = new char[length];
    StringBuilder sb = new StringBuilder();

    username[0] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
    username[1] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
    username[2] = numbers.charAt(random.nextInt(numbers.length()));

    for (int i = 3; i < length; i++) {
      username[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
    }

    for (char c : username) {
      sb.append(c);
    }
    log.info("USERNAME: " + sb);
    return sb.toString();
  }

  public static String randomSetDefaultValuePassword(String text){
    return text + getDateFormat(AppDateFormats.yyMMddHHssPattern) + randomNumber(99);
  }

  public static String randomSetOldPolicyValuePassword(String text){
    return text + getDateFormat(AppDateFormats.yyM) + randomNumber(99);
  }

}
