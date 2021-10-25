package Assignment2.ServerA2;

import static Assignment2.ServerA2.ReadWriteUtility.*;
import static Assignment2.ServerA2.ContentValidationUtility.*;

import ServerLibrary.LiftRide;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "SkiersServletA2", value = "/skiers", asyncSupported = true)
public class SkiersServletA2 extends HttpServlet {


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
            errorMissingParameters(response, writer);
        }

        // switch on the first unique segment if the request is valid send the response otherwise an error
        switch (urlParts[SWITCH_INDEX]){

            case "seasons":

                if (validateLiftRideAndSkierDay(urlParts)){
                    sendSkierDayVertical(response,writer);
                } else {
                    errorInvalidParameters(response, writer);
                }

            case "vertical":
                // create a query hashmap to validate the query portion as well
                HashMap<String, String> queryMap = createQueryMap(request.getQueryString());
                if (queryMap == null) {
                    errorMissingParameters(response, writer);
                }
                if (validateSkierResortTotals(urlParts, queryMap))
                {
                    sendSkierResortTotals(response, writer);
                } else {
                    errorInvalidParameters(response, writer);
                }
            default:
                errorInvalidParameters(response, writer);
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

        if (!validateUrlSize(SEGMENT_COUNT, urlParts)){
            errorMissingParameters(response,writer);
        }
        if (validateLiftRideAndSkierDay(urlParts)) {
            LiftRide liftRide = readLiftRideBody(request.getReader());
            if (liftRide == null || ! liftRide.isValid()) {
                errorInvalidParameters(response,writer);
            }
        } else {
            sendPostSuccess(response,writer);
        }


    }
}
