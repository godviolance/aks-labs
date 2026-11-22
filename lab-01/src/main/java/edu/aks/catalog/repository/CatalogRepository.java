package edu.aks.catalog.repository;

import edu.aks.catalog.model.Course;
import edu.aks.catalog.model.Lesson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class CatalogRepository {
    @PersistenceContext(unitName = "catalog")
    private EntityManager em;

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

    public Course lockCourse(long id) {
        return em.find(Course.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    public Lesson lockLesson(long id) {
        return em.find(Lesson.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    public boolean hasLessons(long courseId) {
        return em.createQuery("SELECT l.id FROM Lesson l WHERE l.course.id = :id", Long.class)
                .setParameter("id", courseId).setMaxResults(1).getResultList().size() > 0;
    }

    public void add(Course course) { em.persist(course); }
    public void add(Lesson lesson) { em.persist(lesson); }
    public void remove(Course course) { em.remove(course); }
    public void remove(Lesson lesson) { em.remove(lesson); }
}
