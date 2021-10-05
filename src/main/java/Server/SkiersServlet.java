package Server;

import java.util.concurrent.FutureTask;
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
        SkiersGetEvaluator skiersGetEvaluator = new SkiersGetEvaluator(request, response, asyncContext);
        FutureTask<Boolean> evaluator = new FutureTask<Boolean>(skiersGetEvaluator);
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
        final AsyncContext asyncContext = request.startAsync(request, response);
        SkiersPostEvaluator skiersPostEvaluator = new SkiersPostEvaluator(request, response, asyncContext);
        FutureTask<Boolean> evaluator = new FutureTask<Boolean>(skiersPostEvaluator);
        threadPool.runOnThread(evaluator);
    }
}
