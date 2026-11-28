package edu.aks.rest.api;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

public final class CatalogDocuments {
    private CatalogDocuments() {}

    public record CourseInput(String title, String description) {}
    public record LessonInput(Long courseId, String topic, Integer minutes) {}

    @JacksonXmlRootElement(localName = "course")
    public record CourseDocument(long id, String title, String description) {}

    @JacksonXmlRootElement(localName = "lesson")
    public record LessonDocument(long id, long courseId, String courseTitle, String topic, int minutes) {}

    @JacksonXmlRootElement(localName = "courses")
    public record CourseList(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "course")
            @com.fasterxml.jackson.annotation.JsonProperty("courses")
            List<CourseDocument> courses
    ) {}

    @JacksonXmlRootElement(localName = "lessons")
    public record LessonList(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "lesson")
            @com.fasterxml.jackson.annotation.JsonProperty("lessons")
            List<LessonDocument> lessons
    ) {}

    @JacksonXmlRootElement(localName = "error")
    public record ApiError(String kind, String message) {}
}
