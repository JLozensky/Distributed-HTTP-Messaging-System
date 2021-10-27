package Server;

import SharedLibrary.LiftRide;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "SkiersServlet", value = "/skiers", asyncSupported = true)
public class SkiersServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // set segment size and index references, inclusive of "skiers" segment
        final int MIN_SEGMENT_COUNT = 3;
        final int SWITCH_INDEX = 2;

        // get the remaining parts of the http request url and make a response Writer
        String[] urlParts = request.getPathInfo().split("/");
        PrintWriter writer = response.getWriter();

        // if there aren't enough url parts to be valid return
        if (MIN_SEGMENT_COUNT > urlParts.length) {
            ReadWriteUtility.errorMissingParameters(response, writer);
        }

        // switch on the first unique segment if the request is valid send the response otherwise an error
        switch (urlParts[SWITCH_INDEX]){

            case "seasons":

                if (ContentValidationUtility.validateLiftRideAndSkierDay(urlParts)){
                    ReadWriteUtility.sendSkierDayVertical(response,writer);
                } else {
                    ReadWriteUtility.errorInvalidParameters(response, writer);
                }

            case "vertical":
                // create a query hashmap to validate the query portion as well
                HashMap<String, String> queryMap = ContentValidationUtility.createQueryMap(request.getQueryString());
                if (queryMap == null) {
                    ReadWriteUtility.errorMissingParameters(response, writer);
                }
                if (ContentValidationUtility.validateSkierResortTotals(urlParts, queryMap))
                {
                    ReadWriteUtility.sendSkierResortTotals(response, writer);
                } else {
                    ReadWriteUtility.errorInvalidParameters(response, writer);
                }
            default:
                ReadWriteUtility.errorInvalidParameters(response, writer);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        // set number of required segments inclusive of "skiers" segment
        final int SEGMENT_COUNT = 8;

        // get the remaining parts of the http request url and make a response Writer
        String[] urlParts = request.getPathInfo().split("/");
        PrintWriter writer = response.getWriter();

        if (!ContentValidationUtility.validateUrlSize(SEGMENT_COUNT, urlParts)){
            ReadWriteUtility.errorMissingParameters(response,writer);
        }
        if (ContentValidationUtility.validateLiftRideAndSkierDay(urlParts)) {
            LiftRide liftRide = ReadWriteUtility.readLiftRideBody(request.getReader());
            if (liftRide == null || ! liftRide.isValid()) {
                ReadWriteUtility.errorInvalidParameters(response,writer);
            }
        } else {
            ReadWriteUtility.sendPostSuccess(response,writer);
        }


    }
}
