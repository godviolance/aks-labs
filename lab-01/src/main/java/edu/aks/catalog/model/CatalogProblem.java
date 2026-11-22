package edu.aks.catalog.model;

import jakarta.ejb.ApplicationException;
import lombok.Getter;

@ApplicationException(rollback = true)
public class CatalogProblem extends RuntimeException {
    @Getter
    private final CatalogProblemKind kind;

    public CatalogProblem(CatalogProblemKind kind, String message) {
        super(message);
        this.kind = kind;
    }
}
