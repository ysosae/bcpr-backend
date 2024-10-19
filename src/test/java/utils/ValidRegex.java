package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class ValidRegex {
  public static final Logger log = Logger.getLogger(ValidRegex.class);
  public static String characterSpecialRegex = "[!@#$%^&*()_+-=[]{};':|,.<>/?]";
  public static final String newPolicyPasswordRegex =
    "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[`!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~]).{12,}$";


  public static boolean isValidRegex(String text, String regex) {
    final Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    return matcher.matches();
  }

}
