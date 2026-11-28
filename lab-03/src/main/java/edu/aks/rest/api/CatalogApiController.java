package edu.aks.rest.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static edu.aks.rest.api.CatalogDocuments.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping(value = "/api", produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
public class CatalogApiController {
    private final CatalogApiService service;

    public CatalogApiController(CatalogApiService service) {
        this.service = service;
    }

    @GetMapping("/courses")
    public CourseList listCourses() { return service.listCourses(); }

    @GetMapping("/lessons")
    public LessonList listLessons(@RequestParam(name = "courseId", required = false) Long courseId) {
        return service.listLessons(courseId);
    }

    @GetMapping("/courses/{id}")
    public CourseDocument readCourse(@PathVariable("id") long id) { return service.readCourse(id); }

    @GetMapping("/lessons/{id}")
    public LessonDocument readLesson(@PathVariable("id") long id) { return service.readLesson(id); }

    @PostMapping(value = "/courses", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    public ResponseEntity<Void> createCourse(@RequestBody CourseInput input) {
        return buildCreatedResponse(service.createCourse(input));
    }

    @PostMapping(value = "/lessons", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    public ResponseEntity<Void> createLesson(@RequestBody LessonInput input) {
        return buildCreatedResponse(service.createLesson(input));
    }

    @PutMapping(value = "/courses/{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateCourse(@PathVariable("id") long id, @RequestBody CourseInput input) {
        service.updateCourse(id, input);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/lessons/{id}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    public ResponseEntity<Void> updateLesson(@PathVariable("id") long id, @RequestBody LessonInput input) {
        service.updateLesson(id, input);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("id") long id) {
        service.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable("id") long id) {
        service.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> buildCreatedResponse(long id) {
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri()).build();
    }
}
