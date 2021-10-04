package Server;

import SharedLibrary.StatusCodes;
import SharedLibrary.ContentValidationUtility;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkierGetEvaluator implements Runnable {

    private HttpServletResponse response;
    private HttpServletRequest request;
    private String resortId = null;
    private ArrayList<String> seasonIds = null;
    private String dayId = null;
    private String skierId = null;
    private String resortName = null;
    private String MISSING_PARAM ="The request is missing parameters";
    private String INVALID_PARAM = "The supplied parameters are invalid"; // planning to turn this into a dynamic
    // message later to inform the client what was incorrect
    private PrintWriter printWriter;

    public SkierGetEvaluator(HttpServletRequest request, HttpServletResponse response){
        this.response = response;
        this.request = request;
        try {
            this.printWriter = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.response.setContentType("application/json");
        String urlPath = this.request.getPathInfo();
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            StatusCodes.setIncorrectPath(this.response);
            this.writeToClient(MISSING_PARAM);
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        this.respond(urlParts);
    }

    private void respond(String[] urlParts) {
                if (urlParts.length < 2) {
                    StatusCodes.setIncorrectPath(this.response);
                    writeToClient(MISSING_PARAM);
                }
                switch (urlParts[2]){
                    case "seasons":
                        if (validateDayVerticalGet(urlParts)){
                            doVerticalDayGet();
                        } else {
                            errorInvalidParameters();
                        }
                        break;
                    case "vertical":
                        if (validateSeasonVerticalGet(urlParts)){
                            doVerticalSeasonGet();
                        } else {
                            errorInvalidParameters();
                        }
                        break;
                    default:
                        errorInvalidParameters();
                        break;
                }
    }

    private void errorInvalidParameters() {
        StatusCodes.setInvalidInputs(this.response);
        this.writeToClient( new Gson().toJson(INVALID_PARAM));
    }

    private void errorMissingParameters() {
        StatusCodes.setIncorrectPath(this.response);
        this.writeToClient(new Gson().toJson(MISSING_PARAM));
    }
    private void doVerticalDayGet() {
        Integer DUMMY_VERT = 697;
        StatusCodes.setRequestSuccess(this.response);
        this.response.setCharacterEncoding("UTF-8");
        String answer = new Gson().toJson(DUMMY_VERT);
        this.writeToClient(answer);
    }

    private void doVerticalSeasonGet(){
        Integer DUMMY_VERT = 539504;
        StatusCodes.setRequestSuccess(this.response);
        this.response.setCharacterEncoding("UTF-8");
        String answer = new Gson().toJson(DUMMY_VERT);
        this.writeToClient(answer);
    }

    public void setResortId(String resortId) {
        this.resortId = resortId;
    }

    public void addSeasonId(String seasonId) {
        if (seasonId == null) {return;}
        this.seasonIds.add(seasonId);
    }

    public void setDayId(String dayId) {
        this.dayId = dayId;
    }

    public void setSkierId(String skierId) {
        this.skierId = skierId;
    }

    public void setResortName(String resortName) {
        this.resortName = resortName;
    }

    /**
     * Validates given url parts according to the getSkierResortTotals request specified in the API linked below
     * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.0.2#/skiers/getSkierResortTotals
     *
     * Sets relevant instance variables if valid
     * @param urlParts a list of all parts included in the URL of the request
     * @return true if it is a valid url request, otherwise false
     */
    private boolean validateSeasonVerticalGet(String[] urlParts) {
        int skierIdIndex = 1;
        String resortParamKey = "resort";
        String seasonParamKey = "season";
        String resName = request.getParameter(resortParamKey);
        this.setResortName(resName);

        if (
            ContentValidationUtility.isSkier(urlParts[skierIdIndex]) &&
            this.resortName != null
        ) {
            this.addSeasonId(request.getParameter("season"));
            return true;
        }
        return false;
    }

    /**
     * Validates given url parts according to the getSkierDayVertical request specified in the API linked below
     * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.0.2#/skiers/getSkierDayVertical
     *
     * Sets instance variables if valid
     * @param urlParts a list of all parts included in the URL of the request
     * @return true if it is a valid url request, otherwise false
     */
    private boolean validateDayVerticalGet(String[] urlParts) {
        int resortIdIndex = 1;
        int seasonIdIndex = 3;
        int daysText = 4;
        int dayIdIndex = 5;
        int skiersText = 6;
        int skierIdIndex = 7;

        if (
            ContentValidationUtility.isResort(urlParts[resortIdIndex]) &&
            ContentValidationUtility.isSeason(urlParts[seasonIdIndex]) &&
            urlParts[daysText].equals("days") &&
            ContentValidationUtility.isDayId(urlParts[dayIdIndex]) &&
            urlParts[skiersText].equals("skiers") &&
            ContentValidationUtility.isSkier(urlParts[skierIdIndex])
        ) {
            this.setResortId(urlParts[resortIdIndex]);
            this.addSeasonId(urlParts[seasonIdIndex]);
            this.setDayId(urlParts[dayIdIndex]);
            this.setSkierId(urlParts[skierIdIndex]);

            return true;
        }
        return false;
    }

    private void writeToClient(String text) {
            this.printWriter.write(text);
//            this.printWriter.flush();
    }

}
