package Server;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResortsPostEvaluator extends Evaluator {


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
        return null;
    }
}
