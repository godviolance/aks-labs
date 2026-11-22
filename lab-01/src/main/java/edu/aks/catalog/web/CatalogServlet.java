package edu.aks.catalog.web;

import edu.aks.catalog.service.Catalog;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("")
public class CatalogServlet extends HttpServlet {
    @EJB
    private Catalog catalog;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setAttribute("courses", catalog.courses());
        req.setAttribute("lessons", catalog.lessons());
        req.getRequestDispatcher("/WEB-INF/catalog.jsp").forward(req, res);
    }
}
