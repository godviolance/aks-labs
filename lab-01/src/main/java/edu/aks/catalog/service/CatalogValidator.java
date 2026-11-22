package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.LessonSchedule;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import static edu.aks.catalog.model.CatalogProblem.Kind.INVALID;

@ApplicationScoped
public class CatalogValidator {
    public String courseTitle(String value) {
        return text(value, "Название", 120, true);
    }

    public String courseDescription(String value) {
        return text(value, "Описание", 1000, false);
    }

    public LessonSchedule lessonSchedule(LessonSchedule schedule) {
        if (schedule.getCourseId() <= 0) {
            throw new CatalogProblem(INVALID, "Выберите курс.");
        }
        if (schedule.getMinutes() < 1 || schedule.getMinutes() > 1440) {
            throw new CatalogProblem(INVALID, "Длительность занятия — от 1 до 1440 минут.");
        }
        String topic = text(schedule.getTopic(), "Тема", 160, true);
        return new LessonSchedule(schedule.getCourseId(), topic, schedule.getMinutes());
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
