package edu.aks.catalog.controller;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.CatalogEvent;
import edu.aks.catalog.dto.CatalogEventInput;
import edu.aks.catalog.service.CatalogFormValidator;
import edu.aks.catalog.service.EventService;
import lombok.RequiredArgsConstructor;
import edu.aks.catalog.service.CatalogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalog;
    private final EventService events;
    private final CatalogFormValidator validator;

    @GetMapping("/")
    public ModelAndView showCatalog(HttpSession session, HttpServletResponse response) {
        synchronized (session) {
            if (session.getAttribute("token") == null) {
                session.setAttribute("token", UUID.randomUUID().toString());
            }
        }
        response.setHeader("Cache-Control", "no-store");
        return buildCatalogView(HttpStatus.OK);
    }

    @PostMapping("/")
    public ModelAndView submitCatalogForm(@RequestParam Map<String, String> form, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String token = session == null ? null : (String) session.getAttribute("token");
        if (!validator.validateCsrfToken(form.get("token"), token)) {
            return new ModelAndView("forbidden", HttpStatus.FORBIDDEN);
        }

        try {
            CatalogEvent event = CatalogEvent.parseAction(form.get("action"));
            events.handleEvent(event, new CatalogEventInput(form.get("id"), form.get("title"),
                    form.get("description"), form.get("courseId"), form.get("topic"), form.get("minutes")));
            RedirectView redirect = new RedirectView("/", true);
            redirect.setStatusCode(HttpStatus.SEE_OTHER);
            return new ModelAndView(redirect);
        } catch (CatalogProblem problem) {
            HttpStatus status = switch (problem.getKind()) {
                case INVALID -> HttpStatus.BAD_REQUEST;
                case MISSING -> HttpStatus.NOT_FOUND;
                case CONFLICT -> HttpStatus.CONFLICT;
            };
            return buildCatalogView(status).addObject("error", problem.getMessage());
        }
    }

    private ModelAndView buildCatalogView(HttpStatus status) {
        return new ModelAndView("catalog", status)
                .addObject("courses", catalog.listCourses())
                .addObject("lessons", catalog.listLessons());
    }
}
