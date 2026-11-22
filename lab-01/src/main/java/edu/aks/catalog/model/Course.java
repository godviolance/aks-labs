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

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
