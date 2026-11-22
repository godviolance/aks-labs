package edu.aks.catalog.model;

import lombok.Value;

@Value
public class LessonSchedule {
    long courseId;
    String topic;
    int minutes;
}
