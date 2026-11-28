package edu.aks.catalog.web;

import edu.aks.catalog.model.LessonSchedule;
import edu.aks.catalog.service.CatalogService;
import org.springframework.stereotype.Component;

@Component
public class EventService {
    private final CatalogService catalog;
    private final CatalogFormValidator validator;

    public EventService(CatalogService catalog, CatalogFormValidator validator) {
        this.catalog = catalog;
        this.validator = validator;
    }

    public void handleEvent(CatalogEvent event, CatalogEventInput input) {
        switch (event) {
            case COURSE_CREATE -> catalog.addCourse(input.title(), input.description());
            case COURSE_UPDATE -> catalog.updateCourse(validator.validateId(input.id()), input.title(), input.description());
            case COURSE_DELETE -> catalog.deleteCourse(validator.validateId(input.id()));
            case LESSON_CREATE -> catalog.addLesson(buildLessonSchedule(input));
            case LESSON_UPDATE -> catalog.updateLesson(validator.validateId(input.id()), buildLessonSchedule(input));
            case LESSON_DELETE -> catalog.deleteLesson(validator.validateId(input.id()));
        }
    }

    private LessonSchedule buildLessonSchedule(CatalogEventInput input) {
        return new LessonSchedule(validator.validateId(input.courseId()), input.topic(),
                validator.validateMinutes(input.minutes()));
    }
}
