package edu.aks.catalog.model;

import jakarta.ejb.ApplicationException;
import lombok.Getter;

@ApplicationException(rollback = true)
public class CatalogProblem extends RuntimeException {
    public enum Kind { INVALID, MISSING, CONFLICT }

    @Getter
    private final Kind kind;

    public CatalogProblem(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }
}
