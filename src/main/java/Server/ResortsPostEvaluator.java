package Server;

import SharedLibrary.Season;
import SharedLibrary.StatusCodes;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResortsPostEvaluator extends ResortsEvaluator {

    private Season season;

    public ResortsPostEvaluator(HttpServletRequest request, HttpServletResponse response, AsyncContext asyncContext) {
        super(request, response, asyncContext);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        if (!super.validateResortSeasonsRequest()){
            return false;
        }
        if (this.validateSeasonValue()){
            return this.doAddSeason();
        } else {
            return super.errorInvalidParameters();
        }
        }

    private Boolean doAddSeason() {
        StatusCodes.setWriteSuccess(super.response);
        super.respondToClient();
        return true;
    }

    private boolean validateSeasonValue() {
        try {
            this.season = gson.fromJson(super.request.getReader(), Season.class);
            System.out.println(this.season.getYear());
            return this.season.isValid();
        } catch (Exception e) {
            return false;
        }
    }
}
