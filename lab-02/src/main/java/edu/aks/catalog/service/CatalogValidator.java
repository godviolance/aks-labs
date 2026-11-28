package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.LessonSchedule;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

import static edu.aks.catalog.model.CatalogProblemKind.INVALID;

@Component
public class CatalogValidator {
    public String validateCourseTitle(String value) {
        return text(value, "Название", 120, true);
    }

    public String validateCourseDescription(String value) {
        return text(value, "Описание", 1000, false);
    }

    public LessonSchedule validateLessonSchedule(LessonSchedule schedule) {
        if (schedule.courseId() <= 0) {
            throw new CatalogProblem(INVALID, "Выберите курс.");
        }
        if (schedule.minutes() < 1 || schedule.minutes() > 1440) {
            throw new CatalogProblem(INVALID, "Длительность занятия — от 1 до 1440 минут.");
        }
        String topic = text(schedule.topic(), "Тема", 160, true);
        return new LessonSchedule(schedule.courseId(), topic, schedule.minutes());
    }

    private String text(String value, String label, int limit, boolean required) {
        String normalized = StringUtils.stripToEmpty(value);
        if (required && StringUtils.isBlank(normalized)) {
            throw new CatalogProblem(INVALID, "Заполните поле «" + label + "».");
        }
        if (normalized.length() > limit) {
            throw new CatalogProblem(INVALID, "Поле «" + label + "» допускает до " + limit + " символов.");
        }
        return normalized;
    }
}
