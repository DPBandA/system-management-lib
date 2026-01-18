/*
System Management (SM)
Copyright (C) 2026  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
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
