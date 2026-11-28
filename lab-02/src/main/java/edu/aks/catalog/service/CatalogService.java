package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.CatalogChange;
import edu.aks.catalog.model.ChangeOperation;
import edu.aks.catalog.model.EntityType;
import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import edu.aks.catalog.model.LessonSchedule;
import edu.aks.catalog.repository.CatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static edu.aks.catalog.model.CatalogProblemKind.CONFLICT;
import static edu.aks.catalog.model.CatalogProblemKind.MISSING;

@Service
@Transactional
public class CatalogService {
    @Autowired
    private CatalogRepository repository;

    @Autowired
    private CatalogValidator validator;

    @Autowired
    private ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public List<Course> listCourses() { return repository.listCourses(); }
    @Transactional(readOnly = true)
    public List<Lesson> listLessons() { return repository.listLessons(); }

    public long addCourse(String title, String description) {
        Course course = new Course(validator.validateCourseTitle(title), validator.validateCourseDescription(description));
        repository.insertCourse(course);
        publishCourseChange(ChangeOperation.INSERT, course);
        return course.getId();
    }

    public void updateCourse(long id, String title, String description) {
        Course course = lockCourseOrThrow(id);
        course.revise(validator.validateCourseTitle(title), validator.validateCourseDescription(description));
        publishCourseChange(ChangeOperation.UPDATE, course);
    }

    public void deleteCourse(long id) {
        Course course = lockCourseOrThrow(id);
        if (repository.existsLessonForCourse(id)) {
            throw new CatalogProblem(CONFLICT, "Сначала перенесите или удалите занятия этого курса.");
        }
        repository.deleteCourse(course);
        publishCourseChange(ChangeOperation.DELETE, course);
    }

    public long addLesson(LessonSchedule input) {
        LessonSchedule schedule = validator.validateLessonSchedule(input);
        Lesson lesson = new Lesson(lockCourseOrThrow(schedule.courseId()), schedule);
        repository.insertLesson(lesson);
        publishLessonChange(ChangeOperation.INSERT, lesson);
        return lesson.getId();
    }

    public void updateLesson(long id, LessonSchedule input) {
        LessonSchedule schedule = validator.validateLessonSchedule(input);
        Course course = lockCourseOrThrow(schedule.courseId());
        Lesson lesson = lockLessonOrThrow(id);
        lesson.reschedule(course, schedule);
        publishLessonChange(ChangeOperation.UPDATE, lesson);
    }

    public void deleteLesson(long id) {
        Lesson lesson = lockLessonOrThrow(id);
        repository.deleteLesson(lesson);
        publishLessonChange(ChangeOperation.DELETE, lesson);
    }

    private void publishCourseChange(ChangeOperation operation, Course course) {
        publishChange(operation, EntityType.COURSE, course.getId(),
                Map.of("title", course.getTitle(), "description", course.getDescription()));
    }

    private void publishLessonChange(ChangeOperation operation, Lesson lesson) {
        publishChange(operation, EntityType.LESSON, lesson.getId(),
                Map.of("topic", lesson.getTopic(), "minutes", Integer.toString(lesson.getMinutes()),
                        "courseId", Long.toString(lesson.getCourse().getId())));
    }

    private void publishChange(ChangeOperation operation, EntityType entity, long id, Map<String, String> details) {
        events.publishEvent(new CatalogChange(UUID.randomUUID(), Instant.now(), operation, entity, id, details));
    }

    private Course lockCourseOrThrow(long id) {
        return repository.findCourseForUpdate(id)
                .orElseThrow(() -> new CatalogProblem(MISSING, "Курс не найден."));
    }

    private Lesson lockLessonOrThrow(long id) {
        return repository.findLessonForUpdate(id)
                .orElseThrow(() -> new CatalogProblem(MISSING, "Занятие не найдено."));
    }
}
