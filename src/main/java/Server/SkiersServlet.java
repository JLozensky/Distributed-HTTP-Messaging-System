package Server;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkiersServlet", value = "/skiers")
public class SkiersServlet extends HttpServlet {
    private ThreadPool threadPool = ThreadPool.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    private boolean validUrl(String[] urlPath) {


        return false;
    }
}
