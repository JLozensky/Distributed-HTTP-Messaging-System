package Server;

import SharedLibrary.StatusCodes;
import SharedLibrary.ContentValidationUtility;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;

public class SkiersGetEvaluator extends SkiersEvaluator {

    private String query;
    private HashMap<String, String> queryMap;
    private String resortName;

    public SkiersGetEvaluator(String urlPath, HttpServletResponse response, String queryString){
        super(urlPath, response);
        this.query = queryString;
    }

    /**
     * The driving logic for evaluating GET requests to the SkiersServlet
     * @return Boolean true if successful operation performed else false
     */
    @Override
    public void run() {
        if (super.urlParts.length < 3) {
            super.errorMissingParameters();
        }
        switch (super.urlParts[2]){

            case "seasons":

                if (validateLiftRideAndSkierDay()){
                    doSkierDayVertical();
                } else {
                    errorInvalidParameters();
                }

            case "vertical":

                this.queryMap = super.createQueryMap(this.query);
                if (this.queryMap == null) {
                    errorMissingParameters();
                }

                if (validateSkierResortTotals(super.urlParts)){
                    doSkierResortTotals();
                } else {
                    super.errorInvalidParameters();
                }

            default:

                super.errorInvalidParameters();
        }
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
    private boolean validateSkierResortTotals(String[] urlParts) {
        int skierIdIndex = 1;
        String resortParamKey = "resort";
        String seasonParamKey = "season";
        if (
            ContentValidationUtility.isSkier(urlParts[skierIdIndex]) &&
            this.queryMap.containsKey(resortParamKey)
        ) {
            this.setResortName(this.queryMap.get(resortParamKey));
            if (queryMap.containsKey(seasonParamKey)) {
            this.addSeasonId(this.queryMap.get(seasonParamKey));
            }
            return true;
        }
        return false;
    }

    /**
     * Performs the execution of a valid skierVerticalSeason GET request
     * @return returns true as this is a successful endpoint of a skier GET request
     */
    private boolean doSkierResortTotals(){
        Integer DUMMY_VERT = 539504;
        StatusCodes.setRequestSuccess(super.response);
        super.writeToClient(super.gson.toJson(DUMMY_VERT));
        return true;
    }

    /**
     * Performs the execution of a valid skierDayVertical GET request
     * @return returns true as this is a successful endpoint of a skier GET request
     */
    private boolean doSkierDayVertical() {
        Integer DUMMY_VERT = 697;
        StatusCodes.setRequestSuccess(super.response);
        super.writeToClient(super.gson.toJson(DUMMY_VERT));
        return true;
    }

}
