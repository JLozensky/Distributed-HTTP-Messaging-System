package Server;

import SharedLibrary.Season;
import SharedLibrary.StatusCodes;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResortsPostEvaluator extends ResortsEvaluator {

    private Season season;

    public ResortsPostEvaluator(String url, HttpServletResponse response, String body) {
        super(url, response, body);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public void run() {
        if (!super.validateResortSeasonsRequest()){
            return;
        }
        if (this.validateSeasonValue()){
            this.doAddSeason();
        } else {
            super.errorInvalidParameters();
        }
        }

    private Boolean doAddSeason() {
        StatusCodes.setWriteSuccess(super.response);
        super.respondToClient();
        return true;
    }

    private boolean validateSeasonValue() {
        try {
            this.season = gson.fromJson(super.body, Season.class);
            System.out.println(this.season.getYear());
            return this.season.isValid();
        } catch (Exception e) {
            return false;
        }
    }
}
