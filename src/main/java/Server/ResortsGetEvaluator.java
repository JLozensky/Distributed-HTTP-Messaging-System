package Server;

import SharedLibrary.Resorts;
import SharedLibrary.Seasons;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResortsGetEvaluator extends ResortsEvaluator {


    public ResortsGetEvaluator(HttpServletRequest request, HttpServletResponse response, AsyncContext asyncContext) {
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
        if (super.urlParts == null) {
            return this.doGetResorts();
        }
        else if (super.validateResortSeasonsRequest()){
            return doGetResortSeasons();
        }
        return false;
    }

    private Boolean doGetResortSeasons() {
        String[] seasons = {"1991", "1996", "2009", "2014"};
        super.writeToClient(super.gson.toJson(new Seasons(seasons)));
        return true;
    }

    private Boolean doGetResorts() {
        super.writeToClient(super.gson.toJson(Resorts.makeDummyResorts()));
        return true;
    }
}
