package Server;

import SharedLibrary.LiftRide;
import SharedLibrary.StatusCodes;
import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkiersPostEvaluator extends SkiersEvaluator {

    private static int urlCountPostSkiDay = 8;
    private LiftRide liftRide;

    public SkiersPostEvaluator(HttpServletRequest request,
        HttpServletResponse response,
        AsyncContext asyncContext) {
        super(request, response, asyncContext);
    }

    /**
     * The driving logic for evaluating POST requests to the SkiersServlet
     *
     * @return true if a successful endpoint was reached else false
     */
    @Override
    public Boolean call() {
        if (!super.validateUrlSize(this.urlCountPostSkiDay)){ return false; }
        if (super.validateLiftRideAndSkierDay() && this.validateWriteLiftRideBody()) {
            return doWriteSkiDay();
        } else {
            return super.errorInvalidParameters();
        }
        }

    private boolean validateWriteLiftRideBody() {
        try {
            this.liftRide = gson.fromJson(super.request.getReader(), LiftRide.class);
            if (!this.liftRide.isValid()) {return false;}
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean doWriteSkiDay() {
        StatusCodes.setWriteSuccess(this.response);
        respondToClient();
        return true;
    }
}

