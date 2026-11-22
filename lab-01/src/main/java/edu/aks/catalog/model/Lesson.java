package edu.aks.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 160)
    private String topic;

    @Column(nullable = false)
    private int minutes;

    protected Lesson() {}

    public Lesson(Course course, String topic, int minutes) {
        schedule(course, topic, minutes);
    }

    public void schedule(Course course, String topic, int minutes) {
        String newTopic = Fields.text(topic, "Тема", 160, true);
        if (course == null) {
            throw new CatalogProblem(CatalogProblem.Kind.INVALID, "Выберите курс.");
        }
        if (minutes < 1 || minutes > 1440) {
            throw new CatalogProblem(CatalogProblem.Kind.INVALID, "Длительность занятия — от 1 до 1440 минут.");
        }
        this.topic = newTopic;
        this.minutes = minutes;
        this.course = course;
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getTopic() { return topic; }
    public int getMinutes() { return minutes; }
}
