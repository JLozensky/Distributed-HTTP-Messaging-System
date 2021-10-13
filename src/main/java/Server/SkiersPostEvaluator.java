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

    public SkiersPostEvaluator(String urlPath, HttpServletResponse response, String body) {
        super(urlPath,response, body);
    }

    /**
     * The driving logic for evaluating POST requests to the SkiersServlet
     *
     * @return true if a successful endpoint was reached else false
     */
    @Override
    public void run() {
        if (!super.validateUrlSize(this.urlCountPostSkiDay)){ return; }
        if (super.validateLiftRideAndSkierDay() && this.validateWriteLiftRideBody()) {
            doWriteSkiDay();
        } else {
            super.errorInvalidParameters();
        }
        }

    private boolean validateWriteLiftRideBody() {
        try {
            this.liftRide = gson.fromJson(super.body, LiftRide.class);
            return this.liftRide.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean doWriteSkiDay() {
        StatusCodes.setWriteSuccess(this.response);
        respondToClient();
        return true;
    }
}

