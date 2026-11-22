package edu.aks.catalog.web;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.service.Catalog;
import java.util.UUID;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        String token = req.getParameter("token");
        if (session == null || token == null || !token.equals(session.getAttribute("token"))) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            String action = req.getParameter("action");
            switch (action == null ? "" : action) {
                case "course-create" -> catalog.addCourse(req.getParameter("title"), req.getParameter("description"));
                case "course-update" -> catalog.updateCourse(id(req, "id"), req.getParameter("title"), req.getParameter("description"));
                case "course-delete" -> catalog.deleteCourse(id(req, "id"));
                case "lesson-create" -> catalog.addLesson(id(req, "courseId"), req.getParameter("topic"), minutes(req));
                case "lesson-update" -> catalog.updateLesson(id(req, "id"), id(req, "courseId"), req.getParameter("topic"), minutes(req));
                case "lesson-delete" -> catalog.deleteLesson(id(req, "id"));
                default -> throw new CatalogProblem(CatalogProblem.Kind.INVALID, "Неизвестное действие.");
            }
            res.setStatus(HttpServletResponse.SC_SEE_OTHER);
            res.setHeader("Location", req.getContextPath() + "/");
        } catch (CatalogProblem problem) {
            res.setStatus(switch (problem.getKind()) {
                case INVALID -> 400;
                case MISSING -> 404;
                case CONFLICT -> 409;
            });
            req.setAttribute("error", problem.getMessage());
            doGet(req, res);
        }
    }

    private static long id(HttpServletRequest req, String field) {
        try {
            long value = Long.parseLong(req.getParameter(field));
            if (value > 0) return value;
        } catch (NumberFormatException ignored) {
            // Report absent and malformed identifiers uniformly.
        }
        throw new CatalogProblem(CatalogProblem.Kind.INVALID, "Некорректный идентификатор.");
    }

    private static int minutes(HttpServletRequest req) {
        try {
            return Integer.parseInt(req.getParameter("minutes"));
        } catch (NumberFormatException e) {
            throw new CatalogProblem(CatalogProblem.Kind.INVALID, "Укажите длительность целым числом.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("token") == null) {
            session.setAttribute("token", UUID.randomUUID().toString());
        }
        res.setHeader("Cache-Control", "no-store");
        req.setAttribute("courses", catalog.courses());
        req.setAttribute("lessons", catalog.lessons());
        req.getRequestDispatcher("/WEB-INF/catalog.jsp").forward(req, res);
    }
}
