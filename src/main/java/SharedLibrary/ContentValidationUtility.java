package SharedLibrary;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import jdk.jfr.StackTrace;
import org.apache.commons.validator.routines.InetAddressValidator;

public final class ContentValidationUtility {

    private ContentValidationUtility(){}

    public static boolean isResort(String resortId) {
        return isInt(resortId);
    }

    public static boolean isSeason(String season) {
        int NUM_DIGITS = 4;
        if (season.length() != NUM_DIGITS || !isInt(season)) {
            return false;
        }
        int seasonYear = Integer.parseInt(season);
        int curYear = Year.now().getValue();
        if (seasonYear > curYear || seasonYear < 0) {
            return false;
        }
        return true;
    }

    public static boolean isSkier(String skierId){
        return isInt(skierId);
    }

    public static boolean numInRange(String num, int mini, int maxi){
        if (!isInt(num)) {
            return false;
        }
        int numInt = Integer.parseInt(num);
        if (numInt < mini || numInt > maxi) {
            return false;
        }
        return true;
    }

    public static boolean isDayId(String day){
        return numInRange(day,1,366);
    }


    private static boolean isInt(String value){
        try {
            Integer.parseInt(value);
        } catch (Exception e){
            return false;
        }
        return true;
    }
    public static boolean validateIP(String ip) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValid(ip);
    }

    public static boolean divisibleBy(int dividend, int divisor){
        return dividend % divisor == 0;
    }

    public static boolean validatePort(String port) {
        return numInRange(port,0,65535);
    }
}
