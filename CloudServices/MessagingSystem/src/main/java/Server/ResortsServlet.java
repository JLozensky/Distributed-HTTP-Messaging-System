package Server;

import static Server.ReadWriteUtility.doGetResorts;
import static Server.ReadWriteUtility.errorInvalidParameters;
import static Server.ReadWriteUtility.readSeasonValue;
import static Server.ReadWriteUtility.sendPostSuccess;

import SharedUtilities.ContentValidationUtility;
import SharedUtilities.RecordCreationUtility;
import SharedUtilities.ResortRecord;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResortsServlet", value = "/resorts")
public class ResortsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // get the remaining parts of the http request url and make a response Writer
        String url = request.getPathInfo();
        PrintWriter writer = response.getWriter();
        String[] urlParts= null;
        // if the url is null then it is a request for all resorts
        if (url == null) {
            doGetResorts(response, writer);
        // otherwise split the pieces, assess request validity, and respond accordingly
        } else {
            urlParts = url.split("/");
        }
        if (urlParts != null && ContentValidationUtility.validateResortUniqueSkiers(urlParts)) {
            ReadWriteUtility.doGetResortUniqueSkiers(response, writer,urlParts);
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

        // Validate request and respond accordingly
        if (! ContentValidationUtility.validateResortSeasonsRequest(urlParts)){
            errorInvalidParameters(response,writer);
        }

        ResortRecord resortRecord = RecordCreationUtility.createResortRecord(urlParts,
            readSeasonValue(request.getReader()));
        if (resortRecord.isValid()){
            sendPostSuccess(response, writer, resortRecord);
        } else {
            errorInvalidParameters(response, writer);
        }

    }
}
