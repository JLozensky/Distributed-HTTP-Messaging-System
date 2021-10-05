package Server;

import SharedLibrary.ContentValidationUtility;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ResortsEvaluator extends Evaluator{
    private String resortId = null;

    public ResortsEvaluator(HttpServletRequest request, HttpServletResponse response,
        AsyncContext asyncContext) {
        super(request, response, asyncContext);
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
