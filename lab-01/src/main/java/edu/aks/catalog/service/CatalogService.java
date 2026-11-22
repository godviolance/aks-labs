package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import edu.aks.catalog.model.LessonSchedule;
import edu.aks.catalog.repository.CatalogRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

import static edu.aks.catalog.model.CatalogProblem.Kind.CONFLICT;
import static edu.aks.catalog.model.CatalogProblem.Kind.MISSING;

@Stateless
public class CatalogService {
    @Inject
    private CatalogRepository repository;

    @Inject
    private CatalogValidator validator;

    public List<Course> courses() { return repository.courses(); }
    public List<Lesson> lessons() { return repository.lessons(); }

    public void addCourse(String title, String description) {
        repository.add(new Course(validator.courseTitle(title), validator.courseDescription(description)));
    }

    public void updateCourse(long id, String title, String description) {
        requireCourse(id).revise(validator.courseTitle(title), validator.courseDescription(description));
    }

    public void deleteCourse(long id) {
        Course course = requireCourse(id);
        if (repository.hasLessons(id)) {
            throw new CatalogProblem(CONFLICT, "Сначала перенесите или удалите занятия этого курса.");
        }
        repository.remove(course);
    }

    public void addLesson(LessonSchedule input) {
        LessonSchedule schedule = validator.lessonSchedule(input);
        repository.add(new Lesson(requireCourse(schedule.getCourseId()), schedule));
    }

    public void updateLesson(long id, LessonSchedule input) {
        LessonSchedule schedule = validator.lessonSchedule(input);
        Course course = requireCourse(schedule.getCourseId());
        requireLesson(id).reschedule(course, schedule);
    }

    public void deleteLesson(long id) {
        repository.remove(requireLesson(id));
    }

    private Course requireCourse(long id) {
        Course course = repository.lockCourse(id);
        if (course == null) {
            throw new CatalogProblem(MISSING, "Курс не найден.");
        }
        return course;
    }

    private Lesson requireLesson(long id) {
        Lesson lesson = repository.lockLesson(id);
        if (lesson == null) {
            throw new CatalogProblem(MISSING, "Занятие не найдено.");
        }
        return lesson;
    }
}
