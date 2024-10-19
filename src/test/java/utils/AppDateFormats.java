package utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;

public class AppDateFormats {
    public static long _startDate = new Date().getTime();
    public static String yyyyMMddPattern = "yyyy-MM-dd";
    public static String yyM = "yyM";
    public static String yyMMddHHssPattern = "yyMMddHHss";
    public static String yyyyMMddTHHmmssSSSZPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static SimpleDateFormat df = new SimpleDateFormat();
    public static DateTime dt = new DateTime();


    private static final SimpleDateFormat yyyyMMddTHHmmssSSSZ = new SimpleDateFormat(yyyyMMddTHHmmssSSSZPattern, Locale.US);

     public static String getTodayDate() {
        _startDate = new Date().getTime();
        return  formatDateForDisplay(_startDate, yyyyMMddTHHmmssSSSZ);
    }

    public static String getTodayDate(String format) {
        _startDate = new Date().getTime();
        df = new SimpleDateFormat(format);
        return df.format(_startDate);
    }

    public static String getDateFormat(String format) {
        _startDate = new Date().getTime();
        df = new SimpleDateFormat(format);
        return df.format(_startDate);
    }

    public static String dateSubtraction(int sDays) {
        dt = new DateTime();
        DateTime dDaysEarlier = dt.minusDays(sDays);
        return dDaysEarlier.toString(yyyyMMddPattern);
    }

    public static String formatDateForDisplay(long date, SimpleDateFormat dateFormat) {
        return dateFormat.format(date);
    }
}
