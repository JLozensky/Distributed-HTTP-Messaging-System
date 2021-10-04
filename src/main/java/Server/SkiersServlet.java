package Server;

import SharedLibrary.StatusCodes;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkiersServlet", value = "/skiers", asyncSupported = true)
public class SkiersServlet extends HttpServlet {
    private ThreadPool threadPool = ThreadPool.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        final AsyncContext asyncContext = request.startAsync(request, response);

        SkierGetEvaluator evaluator = new SkierGetEvaluator(request, response, asyncContext);
//        SkierGetEvaluator evaluator = new SkierGetEvaluator(request.getPathInfo(), request.getQueryString(), response);
        threadPool.runOnThread(evaluator);
//        response.setContentType("plain/text");
//        response.setStatus(200);
//        String boo = request.getParameter("oooo");
//        String yay = request.getParameter("resort");
//        PrintWriter printWriter = response.getWriter();
//        printWriter.print(yay);
//        printWriter.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    private boolean validUrl(String[] urlPath) {


        return false;
    }
}
