package edu.aks.catalog.repository;

import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CatalogRepository {
    private final EntityManager em;

    public List<Course> listCourses() {
        return em.createQuery("SELECT c FROM Course c ORDER BY c.title, c.id", Course.class)
                .getResultList();
    }

    public List<Lesson> listLessons() {
        return em.createQuery("""
                SELECT l FROM Lesson l JOIN FETCH l.course
                ORDER BY l.course.title, l.topic, l.id
                """, Lesson.class).getResultList();
    }

    public List<Lesson> listLessonsForCourse(long courseId) {
        return em.createQuery("""
                SELECT l FROM Lesson l JOIN FETCH l.course
                WHERE l.course.id = :id ORDER BY l.topic, l.id
                """, Lesson.class).setParameter("id", courseId).getResultList();
    }

    public Optional<Course> findCourse(long id) {
        return Optional.ofNullable(em.find(Course.class, id));
    }

    public Optional<Lesson> findLesson(long id) {
        return em.createQuery("SELECT l FROM Lesson l JOIN FETCH l.course WHERE l.id = :id", Lesson.class)
                .setParameter("id", id).getResultList().stream().findFirst();
    }

    public Optional<Course> findCourseForUpdate(long id) {
        return Optional.ofNullable(em.find(Course.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    public Optional<Lesson> findLessonForUpdate(long id) {
        return Optional.ofNullable(em.find(Lesson.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    public boolean existsLessonForCourse(long courseId) {
        return !em.createQuery("SELECT l.id FROM Lesson l WHERE l.course.id = :id", Long.class)
                .setParameter("id", courseId).setMaxResults(1).getResultList().isEmpty();
    }

    public void insertCourse(Course course) { em.persist(course); }
    public void insertLesson(Lesson lesson) { em.persist(lesson); }
    public void deleteCourse(Course course) { em.remove(course); }
    public void deleteLesson(Lesson lesson) { em.remove(lesson); }
}
