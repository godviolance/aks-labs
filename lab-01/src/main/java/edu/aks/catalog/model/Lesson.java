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

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getTopic() { return topic; }
    public int getMinutes() { return minutes; }
}
