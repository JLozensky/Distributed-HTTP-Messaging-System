package Server;

import static Server.ReadWriteUtility.*;

import A1.ServerLibrary.Season;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResortsServlet", value = "/resorts", asyncSupported = true)
public class ResortsServlet extends HttpServlet {

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
        if (urlParts != null && ContentValidationUtility.validateResortSeasonsRequest(urlParts)) {
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

        if (! ContentValidationUtility.validateResortSeasonsRequest(urlParts)){
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
