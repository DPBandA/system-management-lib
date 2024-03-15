package jm.com.dpbennett.sm.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class RegistrationHandler extends HttpServlet {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;

    public EntityManager getEntityManager1() {
        return EMF.createEntityManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");

        String token = request.getParameter("token");

        if (token != null) {
            // tk
            System.out.println("Token: " + token);
            // Send a response back to the client
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h1>Hello Servlet</h1>");
            response.getWriter().println("<p>Token: " + token + "</p>");
            response.getWriter().println("</body></html>");
        } else {
            // tk add page to redirect to with a system option, update release notes.
            response.sendRedirect("http://localhost:8080/sm/index.xhtml");
        }
    }
}
