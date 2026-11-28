package edu.aks.catalog.web;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.service.CatalogService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Dependent
public class CatalogWebService {
    private final CatalogService catalog;
    private final EventService events;
    private final CatalogFormValidator validator;

    @Inject
    public CatalogWebService(CatalogService catalog, EventService events, CatalogFormValidator validator) {
        this.catalog = catalog;
        this.events = events;
        this.validator = validator;
    }

    public void showCatalog(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        synchronized (session) {
            if (session.getAttribute("token") == null) {
                session.setAttribute("token", UUID.randomUUID().toString());
            }
        }
        res.setHeader("Cache-Control", "no-store");
        req.setAttribute("courses", catalog.listCourses());
        req.setAttribute("lessons", catalog.listLessons());
        req.getRequestDispatcher("/WEB-INF/catalog.jsp").forward(req, res);
    }

    public void submitCatalogForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpSession session = req.getSession(false);
        String sessionToken = session == null ? null : (String) session.getAttribute("token");
        if (!validator.validateCsrfToken(req.getParameter("token"), sessionToken)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            CatalogEvent event = CatalogEvent.parseAction(req.getParameter("action"));
            events.handleEvent(event, readFormInput(req));
            res.setStatus(HttpServletResponse.SC_SEE_OTHER);
            res.setHeader("Location", req.getContextPath() + "/");
        } catch (CatalogProblem problem) {
            res.setStatus(switch (problem.getKind()) {
                case INVALID -> HttpServletResponse.SC_BAD_REQUEST;
                case MISSING -> HttpServletResponse.SC_NOT_FOUND;
                case CONFLICT -> HttpServletResponse.SC_CONFLICT;
            });
            req.setAttribute("error", problem.getMessage());
            showCatalog(req, res);
        }
    }

    private CatalogEventInput readFormInput(HttpServletRequest req) {
        return new CatalogEventInput(req.getParameter("id"), req.getParameter("title"),
                req.getParameter("description"), req.getParameter("courseId"),
                req.getParameter("topic"), req.getParameter("minutes"));
    }
}
