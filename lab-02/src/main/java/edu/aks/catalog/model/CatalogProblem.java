package edu.aks.catalog.model;

import lombok.Getter;

public class CatalogProblem extends RuntimeException {
    @Getter
    private final CatalogProblemKind kind;

    public CatalogProblem(CatalogProblemKind kind, String message) {
        super(message);
        this.kind = kind;
    }
}
