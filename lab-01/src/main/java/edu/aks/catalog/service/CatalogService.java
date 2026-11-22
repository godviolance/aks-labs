package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import edu.aks.catalog.model.LessonSchedule;
import edu.aks.catalog.repository.CatalogRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

import static edu.aks.catalog.model.CatalogProblemKind.CONFLICT;
import static edu.aks.catalog.model.CatalogProblemKind.MISSING;

@Stateless
public class CatalogService {
    @Inject
    private CatalogRepository repository;

    @Inject
    private CatalogValidator validator;

    public List<Course> listCourses() { return repository.listCourses(); }
    public List<Lesson> listLessons() { return repository.listLessons(); }

    public void addCourse(String title, String description) {
        repository.insertCourse(new Course(validator.validateCourseTitle(title), validator.validateCourseDescription(description)));
    }

    public void updateCourse(long id, String title, String description) {
        lockCourseOrThrow(id).revise(validator.validateCourseTitle(title), validator.validateCourseDescription(description));
    }

    public void deleteCourse(long id) {
        Course course = lockCourseOrThrow(id);
        if (repository.existsLessonForCourse(id)) {
            throw new CatalogProblem(CONFLICT, "Сначала перенесите или удалите занятия этого курса.");
        }
        repository.deleteCourse(course);
    }

    public void addLesson(LessonSchedule input) {
        LessonSchedule schedule = validator.validateLessonSchedule(input);
        repository.insertLesson(new Lesson(lockCourseOrThrow(schedule.courseId()), schedule));
    }

    public void updateLesson(long id, LessonSchedule input) {
        LessonSchedule schedule = validator.validateLessonSchedule(input);
        Course course = lockCourseOrThrow(schedule.courseId());
        lockLessonOrThrow(id).reschedule(course, schedule);
    }

    public void deleteLesson(long id) {
        repository.deleteLesson(lockLessonOrThrow(id));
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
