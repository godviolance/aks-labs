package edu.aks.rest.api;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.CatalogProblemKind;
import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import edu.aks.catalog.model.LessonSchedule;
import edu.aks.catalog.repository.CatalogRepository;
import edu.aks.catalog.service.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static edu.aks.rest.api.CatalogDocuments.*;

@Service
public class CatalogApiService {
    private final CatalogService catalog;
    private final CatalogRepository repository;

    public CatalogApiService(CatalogService catalog, CatalogRepository repository) {
        this.catalog = catalog;
        this.repository = repository;
    }

    public CourseList listCourses() {
        return new CourseList(catalog.listCourses().stream().map(this::buildCourseDocument).toList());
    }

    @Transactional(readOnly = true)
    public LessonList listLessons(Long courseId) {
        var lessons = courseId == null ? catalog.listLessons() : repository.listLessonsForCourse(courseId);
        return new LessonList(lessons.stream().map(this::buildLessonDocument).toList());
    }

    @Transactional(readOnly = true)
    public CourseDocument readCourse(long id) {
        return repository.findCourse(id).map(this::buildCourseDocument)
                .orElseThrow(() -> new CatalogProblem(CatalogProblemKind.MISSING, "Курс не найден."));
    }

    @Transactional(readOnly = true)
    public LessonDocument readLesson(long id) {
        return repository.findLesson(id).map(this::buildLessonDocument)
                .orElseThrow(() -> new CatalogProblem(CatalogProblemKind.MISSING, "Занятие не найдено."));
    }

    public long createCourse(CourseInput input) {
        return catalog.addCourse(input.title(), input.description());
    }

    public void updateCourse(long id, CourseInput input) {
        catalog.updateCourse(id, input.title(), input.description());
    }

    public void deleteCourse(long id) {
        catalog.deleteCourse(id);
    }

    public long createLesson(LessonInput input) {
        return catalog.addLesson(buildSchedule(input));
    }

    public void updateLesson(long id, LessonInput input) {
        catalog.updateLesson(id, buildSchedule(input));
    }

    public void deleteLesson(long id) {
        catalog.deleteLesson(id);
    }

    private LessonSchedule buildSchedule(LessonInput input) {
        return new LessonSchedule(input.courseId() == null ? 0 : input.courseId(), input.topic(),
                input.minutes() == null ? 0 : input.minutes());
    }

    private CourseDocument buildCourseDocument(Course course) {
        return new CourseDocument(course.getId(), course.getTitle(), course.getDescription());
    }

    private LessonDocument buildLessonDocument(Lesson lesson) {
        Course course = lesson.getCourse();
        return new LessonDocument(lesson.getId(), course.getId(), course.getTitle(),
                lesson.getTopic(), lesson.getMinutes());
    }
}
