package edu.aks.catalog.model;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CatalogProblem extends RuntimeException {
    public enum Kind { INVALID, MISSING, CONFLICT }

    private final Kind kind;

    public CatalogProblem(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind getKind() { return kind; }
}
