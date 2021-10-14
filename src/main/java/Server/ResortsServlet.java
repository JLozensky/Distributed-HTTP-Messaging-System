package Server;

import static Server.ContentValidationUtility.validateResortSeasonsRequest;
import static Server.ReadWriteUtility.doGetResortSeasons;
import static Server.ReadWriteUtility.doGetResorts;
import static Server.ReadWriteUtility.errorInvalidParameters;
import static Server.ReadWriteUtility.readSeasonValue;
import static Server.ReadWriteUtility.sendPostSuccess;

import SharedLibrary.Season;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "ResortsServlet", value = "/resorts", asyncSupported = true)
public class ResortsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // get the remaining parts of the http request url and make a response Writer
        String[] urlParts = request.getPathInfo().split("/");
        PrintWriter writer = response.getWriter();

        if (urlParts == null) {
            doGetResorts(response, writer);
        } else if (validateResortSeasonsRequest(urlParts)) {
            doGetResortSeasons(response, writer);
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
