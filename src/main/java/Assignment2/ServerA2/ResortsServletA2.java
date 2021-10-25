package Assignment2.ServerA2;

import static Assignment2.ServerA2.ReadWriteUtility.*;
import static Assignment2.ServerA2.ContentValidationUtility.*;

import ServerLibrary.Season;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResortsServletA2", value = "/resorts", asyncSupported = true)
public class ResortsServletA2 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // get the remaining parts of the http request url and make a response Writer
        String url = request.getPathInfo();
        PrintWriter writer = response.getWriter();
        String[] urlParts= null;
        if (url == null) {
            doGetResorts(response, writer);
        } else {
            urlParts = url.split("/");
        }
        if (urlParts != null && validateResortSeasonsRequest(urlParts)) {
            doGetResortSeasons(response, writer);
        } else {
            errorInvalidParameters(response,writer);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // get the remaining parts of the http request url and make a response Writer
        String[] urlParts = request.getPathInfo().split("/");
        PrintWriter writer = response.getWriter();

        if (! validateResortSeasonsRequest(urlParts)){
            errorInvalidParameters(response,writer);
        }
        Season season = readSeasonValue(request.getReader());
        if (season.isValid()){
            sendPostSuccess(response, writer);
        } else {
            errorInvalidParameters(response, writer);
        }

    }
}
