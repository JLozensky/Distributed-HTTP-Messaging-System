package Server;

import SharedLibrary.InterfaceSkierDataObject;
import SharedLibrary.LiftRide;
import SharedLibrary.Resorts;
import SharedLibrary.Season;
import SharedLibrary.Seasons;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public class ReadWriteUtility {
    private static String MISSING_PARAM ="The request is missing parameters";
    private static String INVALID_PARAM = "The supplied parameters are invalid";
    private static Gson gson = new Gson();

    /**
     * Sets a 401 response code and sends the response to the original requester
     * @return false as this is an unsuccessful endpoint for an evaluation function
     */
    public static void errorMissingParameters(HttpServletResponse response, PrintWriter writer) {
        StatusCodes.setIncorrectPath(response);
        writeToClient(response,writer, new ErrorBuilder(MISSING_PARAM));

    }
    /**
     * Sets a 400 response code and sends the response to the original requester
     * @return false as this is an unsuccessful endpoint for an evaluation function
     */
    public static void errorInvalidParameters(HttpServletResponse response , PrintWriter writer) {
        StatusCodes.setInvalidInputs(response);
        writeToClient(response,writer, new ErrorBuilder(INVALID_PARAM));
    }

    /**
     * Makes the String response for a valid skierVerticalSeason GET request
     * @return the value from a successful skierVerticalSeason GET request
     */
    public static void sendSkierResortTotals(HttpServletResponse response, PrintWriter writer){
        Integer DUMMY_VERT = 539504;
        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer,DUMMY_VERT);
    }

    /**
     * Makes the String response for a valid skierDayVertical GET request
     * @return the value from a successful endpoint of a skier GET request
     */
    public static void sendSkierDayVertical(HttpServletResponse response, PrintWriter writer) {
        Integer DUMMY_VERT = 697;
        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer,DUMMY_VERT);
    }


    public static void writeToClient(HttpServletResponse response, PrintWriter writer, Object message) {
        response.setContentType("application/json");
        writer.write(gson.toJson(message));
        writer.flush();
        writer.close();
    }

    public static void sendPostSuccess(HttpServletResponse response, PrintWriter writer,
        InterfaceSkierDataObject dataObject) {
        if (SqsSend.getInstance().sendMessage(gson.toJson(dataObject))) {

            StatusCodes.setWriteSuccess(response);
            writer.flush();
            writer.close();
        }
        else {
            errorProcessing(response, writer);
        }
    }

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

    public static void doGetResorts(HttpServletResponse response, PrintWriter writer) {
        StatusCodes.setRequestSuccess(response);
        writeToClient(response,writer, Resorts.makeDummyResorts());
    }
}
