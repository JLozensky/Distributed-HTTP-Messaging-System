package Server;

import java.util.concurrent.FutureTask;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "ResortsServlet", value = "/resorts", asyncSupported = true)
public class ResortsServlet extends HttpServlet {
    private ThreadPool threadPool = ThreadPool.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        ResortsGetEvaluator resortsGetEvaluator = new ResortsGetEvaluator(request.getPathInfo(), response);
        resortsGetEvaluator.run();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {


        ResortsPostEvaluator resortsPostEvaluator = new ResortsPostEvaluator(request.getPathInfo(), response,
            request.getQueryString());
        resortsPostEvaluator.run();

    }
}
