package edu.aks.catalog.model;

import static edu.aks.catalog.model.CatalogProblem.Kind.INVALID;

final class Fields {
    private Fields() {}

    static String text(String value, String label, int limit, boolean required) {
        String text = value == null ? "" : value.strip();
        if (required && text.isBlank()) {
            throw new CatalogProblem(INVALID, "Заполните поле «" + label + "».");
        }
        if (text.length() > limit) {
            throw new CatalogProblem(INVALID, "Поле «" + label + "» допускает до " + limit + " символов.");
        }
        return text;
    }
}
