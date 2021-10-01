package SharedLibrary;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import jdk.jfr.StackTrace;

public final class contentValidationUtility {

    private contentValidationUtility(){}

    public static boolean isResort(String resortId) {
        return isInt(resortId);
    }

    public static boolean isSeason(String season) {
        int NUM_DIGITS = 4;
        if (season.length() != NUM_DIGITS || !isInt(season)) {
            return false;
        }
        int seasonYear = Integer.parseInt(season);
        int curYear = Calendar.YEAR;
        if (seasonYear > curYear || seasonYear < 0) {
            return false;
        }
        return true;
    }

    public static boolean isSkier(String skierId){
        return isInt(skierId);
    }

    public static boolean isDayId(String day){
        int MAX_DAYS = 366;

        if (!isInt(day)) {
            return false;
        }

        int dayInt = Integer.parseInt(day);
        if (dayInt < 1 || dayInt > MAX_DAYS) {
            return false;
        }
        return true;
    }


    private static boolean isInt(String value){
        try {
            Integer.parseInt(value);
        } catch (Exception e){
            return false;
        }
        return true;
    }



}
