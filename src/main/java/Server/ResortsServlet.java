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
        final AsyncContext asyncContext = request.startAsync(request, response);

        ResortsGetEvaluator resortsGetEvaluator = new ResortsGetEvaluator(request, response, asyncContext);
        FutureTask<Boolean> evaluator = new FutureTask<Boolean>(resortsGetEvaluator);
        threadPool.runOnThread(evaluator);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        final AsyncContext asyncContext = request.startAsync(request, response);

        ResortsPostEvaluator resortsPostEvaluator = new ResortsPostEvaluator(request, response, asyncContext);
        FutureTask<Boolean> evaluator = new FutureTask<Boolean>(resortsPostEvaluator);
        threadPool.runOnThread(evaluator);
    }
}
