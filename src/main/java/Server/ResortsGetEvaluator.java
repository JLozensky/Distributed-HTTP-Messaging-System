package Server;

import SharedLibrary.Resorts;
import SharedLibrary.Seasons;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResortsGetEvaluator extends ResortsEvaluator {


    public ResortsGetEvaluator(String urlPath, HttpServletResponse response) {
        super(urlPath,response);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public void run()  {
        if (super.urlParts == null) {
            this.doGetResorts();
        }
        else if (super.validateResortSeasonsRequest()){
            doGetResortSeasons();
        }
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
