package edu.aks.rest.api;

import org.springframework.web.bind.annotation.*;
import tools.jackson.dataformat.xml.XmlMapper;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@RestController
@RequestMapping(value = "/api", params = "format=html", produces = "text/html;charset=UTF-8")
public class CatalogHtmlController {
    private final CatalogApiService service;
    private final XmlMapper mapper = XmlMapper.builder().build();
    private final Templates stylesheet;

    public CatalogHtmlController(CatalogApiService service) throws TransformerException, IOException {
        this.service = service;
        TransformerFactory factory = TransformerFactory.newDefaultInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        try (var source = CatalogHtmlController.class.getResourceAsStream("/static/catalog.xsl")) {
            if (source == null) {
                throw new IOException("Catalog stylesheet not found");
            }
            stylesheet = factory.newTemplates(new StreamSource(source));
        }
    }

    @GetMapping("/courses")
    public String showCourses() throws TransformerException {
        return renderHtml(service.listCourses());
    }

    @GetMapping("/courses/{id}")
    public String showCourse(@PathVariable("id") long id) throws TransformerException {
        return renderHtml(service.readCourse(id));
    }

    @GetMapping("/lessons")
    public String showLessons(@RequestParam(name = "courseId", required = false) Long courseId)
            throws TransformerException {
        return renderHtml(service.listLessons(courseId));
    }

    @GetMapping("/lessons/{id}")
    public String showLesson(@PathVariable("id") long id) throws TransformerException {
        return renderHtml(service.readLesson(id));
    }

    private String renderHtml(Object document) throws TransformerException {
        var transformer = stylesheet.newTransformer();
        transformer.setParameter("format", "html");
        var html = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(mapper.writeValueAsString(document))),
                new StreamResult(html));
        return html.toString();
    }
}
