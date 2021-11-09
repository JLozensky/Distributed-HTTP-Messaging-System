package Server;

import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "HealthServlet", value = "/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        StatusCodes.setRequestSuccess(response);
        writer.flush();
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        StatusCodes.setRequestSuccess(response);
        writer.flush();
        writer.close();
    }
}
