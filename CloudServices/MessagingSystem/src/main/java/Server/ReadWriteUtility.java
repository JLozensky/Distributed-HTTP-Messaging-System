package Server;

import SharedUtilities.LiftRide;
import SharedUtilities.ResortRecord;
import SharedUtilities.Resorts;
import SharedUtilities.Season;
import SharedUtilities.Seasons;
import SharedUtilities.SkierRecord;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;

public class ReadWriteUtility {
    private static String MISSING_PARAM ="The request is missing parameters";
    private static String INVALID_PARAM = "The supplied parameters are invalid";
    private static Gson gson = new Gson();

    /**
     * Sets a 401 response code and sends the response to the original requester
     */
    public static void errorMissingParameters(HttpServletResponse response, PrintWriter writer) {
        StatusCodes.setIncorrectPath(response);
        writeToClient(response,writer, new ErrorBuilder(MISSING_PARAM));

    }
    /**
     * Sets a 400 response code and sends the response to the original requester
     */
    public static void errorInvalidParameters(HttpServletResponse response , PrintWriter writer) {
        StatusCodes.setInvalidInputs(response);
        writeToClient(response,writer, new ErrorBuilder(INVALID_PARAM));
    }

    /**
     * Makes the String response for a valid skierVerticalSeason GET request
     */
    public static void sendSkierResortTotals(HttpServletResponse response, PrintWriter writer, String[] urlParts,
        HashMap<String, String> queryMap) {
        final int skierIdIndex = 1;
        final String resortParamKey = "resort";
        final String seasonParamKey = "season";
        Integer vert;

        if (queryMap.containsKey("season")){
            vert = DatabaseReader.getSkierTotalVert(
                urlParts[skierIdIndex],
                queryMap.get(resortParamKey),
                queryMap.get(seasonParamKey));
        } else {
            vert = DatabaseReader.getSkierTotalVert(
                urlParts[skierIdIndex],
                queryMap.get(resortParamKey));
        }

        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer,vert);
    }

    /**
     * Makes the String response for a valid skierDayVertical GET request
     */
    public static void sendSkierDayVertical(HttpServletResponse response, PrintWriter writer, String[] urlParts) {
        int resortIdIndex = 1;
        int seasonIdIndex = 3;
        int dayIdIndex = 5;
        int skierIdIndex = 6;

        Integer vert = DatabaseReader.getSkierTotalVertDay(
            urlParts[skierIdIndex],
            urlParts[resortIdIndex],
            urlParts[seasonIdIndex],
            urlParts[dayIdIndex]
        );

        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer,vert);
    }


    public static void writeToClient(HttpServletResponse response, PrintWriter writer, Object message) {
        response.setContentType("application/json");
        writer.write(gson.toJson(message));
        writer.flush();
        writer.close();
    }

    public static void sendPostSuccess(HttpServletResponse response, PrintWriter writer,
        ResortRecord record) {
        if (SqsSend.getInstance().sendMessage(gson.toJson(record))) {
            StatusCodes.setWriteSuccess(response);
            writer.flush();
            writer.close();
        }
        else {
            errorProcessing(response, writer);
        }
    }

    public static void sendPostSuccess(HttpServletResponse response, PrintWriter writer,
        SkierRecord record) {
        if (SqsSend.getInstance().sendMessage(gson.toJson(record))) {
            StatusCodes.setWriteSuccess(response);
            writer.flush();
            writer.close();
        }
        else {
            errorProcessing(response, writer);
        }
    }
    // Placeholder for future logging
    private static void errorProcessing(HttpServletResponse response, PrintWriter writer) {

    }

    public static LiftRide readLiftRideBody(BufferedReader reader) {
        try {
            return gson.fromJson(reader, LiftRide.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static Season readSeasonValue(BufferedReader reader) {
        try {
            return gson.fromJson(reader, Season.class);
        } catch (Exception e) {
            return null;
        }
    }


    public static void doGetResortSeasons(HttpServletResponse response, PrintWriter writer) {
        StatusCodes.setRequestSuccess(response);
        writeToClient(response, writer, Seasons.makeDummySeasons());

    }

    public static void doGetResortUniqueSkiers(HttpServletResponse response, PrintWriter writer, String[] urlParts){
        final int resortIdIndex = 1;
        final int seasonsIdIndex = 3;
        final int dayIdIndex = 5;

        int numSkiers = DatabaseReader.getResortUniqueSkierDay(
            urlParts[resortIdIndex],
            urlParts[seasonsIdIndex],
            urlParts[dayIdIndex]);

        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer,new ResortGetUniqueSkiers(urlParts[resortIdIndex], numSkiers));

    }

    public static void doGetSkierVert(HttpServletResponse response, PrintWriter writer, String[] urlParts){}

    public static void doGetSkierVertDay(HttpServletResponse response, PrintWriter writer, String[] urlParts){}


    public static void doGetResorts(HttpServletResponse response, PrintWriter writer) {
        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer, Resorts.makeDummyResorts());
    }
}
