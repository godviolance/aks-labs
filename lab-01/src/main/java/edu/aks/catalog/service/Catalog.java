package edu.aks.catalog.service;

import edu.aks.catalog.model.Course;
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
