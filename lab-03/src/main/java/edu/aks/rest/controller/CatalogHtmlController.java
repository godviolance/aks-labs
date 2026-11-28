package edu.aks.rest.controller;

import org.springframework.web.bind.annotation.*;
import edu.aks.rest.service.CatalogApiService;
import edu.aks.rest.service.CatalogStylesheetService;
import lombok.RequiredArgsConstructor;
import javax.xml.transform.TransformerException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", params = "format=html", produces = "text/html;charset=UTF-8")
public class CatalogHtmlController {
    private final CatalogApiService service;
    private final CatalogStylesheetService stylesheet;

    @GetMapping("/courses")
    public String showCourses() throws TransformerException {
        return stylesheet.renderHtml(service.listCourses());
    }

    @GetMapping("/courses/{id}")
    public String showCourse(@PathVariable("id") long id) throws TransformerException {
        return stylesheet.renderHtml(service.readCourse(id));
    }

    @GetMapping("/lessons")
    public String showLessons(@RequestParam(name = "courseId", required = false) Long courseId)
            throws TransformerException {
        return stylesheet.renderHtml(service.listLessons(courseId));
    }

    @GetMapping("/lessons/{id}")
    public String showLesson(@PathVariable("id") long id) throws TransformerException {
        return stylesheet.renderHtml(service.readLesson(id));
    }

}
