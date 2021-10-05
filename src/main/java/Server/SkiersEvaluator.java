package Server;

import SharedLibrary.ContentValidationUtility;
import java.util.ArrayList;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SkiersEvaluator extends Evaluator {
    protected ArrayList<String> seasonIds;
    private String resortId = null;
    private String dayId = null;
    private String skierId = null;

    public SkiersEvaluator(HttpServletRequest request, HttpServletResponse response,
        AsyncContext asyncContext) {
        super(request, response, asyncContext);
        this.seasonIds = new ArrayList<>();
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
    protected boolean validateLiftRideAndSkierDay() {
        if (super.urlParts.length < 8){
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
            ContentValidationUtility.isResort(super.urlParts[resortIdIndex]) &&
                super.urlParts[seasonsText].equals("seasons") &&
                ContentValidationUtility.isSeason(super.urlParts[seasonIdIndex]) &&
                super.urlParts[daysText].equals("days") &&
                ContentValidationUtility.isDayId(super.urlParts[dayIdIndex]) &&
                super.urlParts[skiersText].equals("skiers") &&
                ContentValidationUtility.isSkier(super.urlParts[skierIdIndex])
        ) {
            this.setResortId(urlParts[resortIdIndex]);
            this.addSeasonId(urlParts[seasonIdIndex]);
            this.setDayId(urlParts[dayIdIndex]);
            this.setSkierId(urlParts[skierIdIndex]);

            return true;
        }
        return false;
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

}
