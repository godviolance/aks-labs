package edu.aks.catalog.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public Lesson(Course course, LessonSchedule schedule) {
        reschedule(course, schedule);
    }

    public void reschedule(Course course, LessonSchedule schedule) {
        this.topic = schedule.topic();
        this.minutes = schedule.minutes();
        this.course = course;
    }
}
