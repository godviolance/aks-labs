package edu.aks.catalog.model;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.CatalogProblemKind;

public enum CatalogEvent {
    COURSE_CREATE("course-create"),
    COURSE_UPDATE("course-update"),
    COURSE_DELETE("course-delete"),
    LESSON_CREATE("lesson-create"),
    LESSON_UPDATE("lesson-update"),
    LESSON_DELETE("lesson-delete");

    private final String action;

    CatalogEvent(String action) {
        this.action = action;
    }

    public static CatalogEvent parseAction(String action) {
        for (CatalogEvent event : values()) {
            if (event.action.equals(action)) {
                return event;
            }
        }
        throw new CatalogProblem(CatalogProblemKind.INVALID, "Неизвестное действие.");
    }
}
