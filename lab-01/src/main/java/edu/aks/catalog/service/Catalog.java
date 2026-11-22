package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.Course;
import jakarta.persistence.LockModeType;
import static edu.aks.catalog.model.CatalogProblem.Kind.*;
import edu.aks.catalog.model.Lesson;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class Catalog {
    @PersistenceContext(unitName = "catalog")
    private EntityManager em;

    public void addCourse(String title, String description) {
        em.persist(new Course(title, description));
    }

    public void updateCourse(long id, String title, String description) {
        courseForWrite(id).revise(title, description);
    }

    public void deleteCourse(long id) {
        Course course = courseForWrite(id);
        long lessons = em.createQuery("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (lessons > 0) {
            throw new CatalogProblem(CONFLICT, "Сначала перенесите или удалите занятия этого курса.");
        }
        em.remove(course);
    }

    public void addLesson(long courseId, String topic, int minutes) {
        em.persist(new Lesson(courseForWrite(courseId), topic, minutes));
    }

    public void updateLesson(long id, long courseId, String topic, int minutes) {
        Course target = courseForWrite(courseId);
        lessonForWrite(id).schedule(target, topic, minutes);
    }

    public void deleteLesson(long id) {
        em.remove(lessonForWrite(id));
    }

    private Course courseForWrite(long id) {
        Course course = em.find(Course.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (course == null) {
            throw new CatalogProblem(MISSING, "Курс не найден.");
        }
        return course;
    }

    private Lesson lessonForWrite(long id) {
        Lesson lesson = em.find(Lesson.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (lesson == null) {
            throw new CatalogProblem(MISSING, "Занятие не найдено.");
        }
        return lesson;
    }

    public List<Course> courses() {
        return em.createQuery("SELECT c FROM Course c ORDER BY c.title, c.id", Course.class)
                .getResultList();
    }

    public List<Lesson> lessons() {
        return em.createQuery("""
                SELECT l FROM Lesson l JOIN FETCH l.course
                ORDER BY l.course.title, l.topic, l.id
                """, Lesson.class).getResultList();
    }
}
