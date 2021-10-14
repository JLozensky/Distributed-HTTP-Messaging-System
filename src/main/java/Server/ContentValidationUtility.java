package Server;

import java.time.Year;
import java.util.HashMap;
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
        if (ip.equals("localhost")) {return true;}

        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValid(ip);
    }

    public static boolean divisibleBy(int dividend, int divisor){
        return dividend % divisor == 0;
    }



    public static boolean validatePort(String port) {
        return numInRange(port,0,65535);
    }

    public static HashMap<String,String> createQueryMap(String string) {
        if (string == null || string.isEmpty() || string.isBlank()){
            return null;
        }
        String[] queryParts = string.split("=");
        HashMap<String,String> queryMap = new HashMap<>();
        String curKey = "";
        for (int i = 0; i < queryParts.length; i++) {
            if (i % 2 == 0) {
                curKey = queryParts[i];
            } else {
                queryMap.put(curKey,queryParts[i]);
            }
        }
        return queryMap;
    }

    public static boolean validateUrlSize(int numSegmentsRequired, String[] urlParts) {
        if (urlParts == null) {return numSegmentsRequired == 0;}
        if (urlParts.length == numSegmentsRequired) { return true; }
        return false;
    }

    /**
     * Validates given url parts according to both the getSkierDayVertical GET request and the writeNewLiftRide POST
     * request specified respectively in the API links below
     *
     * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.0.2#/skiers/getSkierDayVertical
     * https://app.swaggerhub.com/#/skiers/writeNewLiftRide
     *
     * Sets instance variables if valid
     * @return true if it is a valid url request, otherwise false
     */
    public static boolean validateLiftRideAndSkierDay(String[] urlParts) {
        if (!validateUrlSize(8, urlParts)){
            return false;
        }
        int resortIdIndex = 1;
        int seasonsText = 2;
        int seasonIdIndex = 3;
        int daysText = 4;
        int dayIdIndex = 5;
        int skiersText = 6;
        int skierIdIndex = 7;
        if (
            ContentValidationUtility.isResort(urlParts[resortIdIndex]) &&
                urlParts[seasonsText].equals("seasons") &&
                ContentValidationUtility.isSeason(urlParts[seasonIdIndex]) &&
                urlParts[daysText].equals("days") &&
                ContentValidationUtility.isDayId(urlParts[dayIdIndex]) &&
                urlParts[skiersText].equals("skiers") &&
                ContentValidationUtility.isSkier(urlParts[skierIdIndex])
        ) {
//            this.setResortId(urlParts[resortIdIndex]);
//            this.addSeasonId(urlParts[seasonIdIndex]);
//            this.setDayId(urlParts[dayIdIndex]);
//            this.setSkierId(urlParts[skierIdIndex]);
            return true;
        }
        return false;
    }

    public static boolean validateResortSeasonsRequest(String[] urlParts) {
        if (!validateUrlSize(3, urlParts)){ return false;}

        int resortIdIndex = 1;
        int seasonsTextIndex = 2;

        if (
            ContentValidationUtility.isResort(urlParts[resortIdIndex]) &&
                urlParts[seasonsTextIndex].equals("seasons")
        ) {
            return true;
        }
        return false;
    }

    /**
     * Validates given url parts according to the getSkierResortTotals request specified in the API linked below
     * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.0.2#/skiers/getSkierResortTotals
     *
     * Sets relevant instance variables if valid
     * @param urlParts a list of all parts included in the URL of the request
     * @return true if it is a valid url request, otherwise false
     */
    public static boolean validateSkierResortTotals(String[] urlParts, HashMap<String, String> queryMap) {
        int skierIdIndex = 1;
        String resortParamKey = "resort";
        String seasonParamKey = "season";
        if (
            ContentValidationUtility.isSkier(urlParts[skierIdIndex]) &&
                queryMap.containsKey(resortParamKey)
        ) {
            // do something with resort value
            if (queryMap.containsKey(seasonParamKey)) {
                // do something with season value
            }
            return true;
        }
        return false;
    }



}
