package Server;

import SharedLibrary.ContentValidationUtility;
import javax.servlet.http.HttpServletResponse;

public abstract class ResortsEvaluator extends Evaluator{
    private String resortId = null;

    public ResortsEvaluator(String urlPath, HttpServletResponse response) {
        super(urlPath,response);

    }

    public ResortsEvaluator(String urlPath, HttpServletResponse response, String body) {
        super(urlPath,response, body);

    }

    protected boolean validateResortSeasonsRequest() {
        if (!super.validateUrlSize(3)){ return false;}

        int resortIdIndex = 1;
        int seasonsTextIndex = 2;

        if (
            ContentValidationUtility.isResort(super.urlParts[resortIdIndex]) &&
            super.urlParts[seasonsTextIndex].equals("seasons")
        ){
            this.resortId = super.urlParts[resortIdIndex];
        } else {
            super.errorInvalidParameters();
            return false;
        }
        return true;
    }

}
