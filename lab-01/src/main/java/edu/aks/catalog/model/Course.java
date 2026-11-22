package edu.aks.catalog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    protected Course() {}

    public Course(String title, String description) {
        revise(title, description);
    }

    public void revise(String title, String description) {
        String newTitle = Fields.text(title, "Название", 120, true);
        String newDescription = Fields.text(description, "Описание", 1000, false);
        this.title = newTitle;
        this.description = newDescription;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
