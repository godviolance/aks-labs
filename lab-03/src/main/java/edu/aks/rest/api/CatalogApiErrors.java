package edu.aks.rest.api;

import edu.aks.catalog.model.CatalogProblem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CatalogApiController.class)
public class CatalogApiErrors {
    @ExceptionHandler(CatalogProblem.class)
    public ResponseEntity<CatalogDocuments.ApiError> handleCatalogProblem(CatalogProblem problem) {
        int status = switch (problem.getKind()) {
            case INVALID -> 400;
            case MISSING -> 404;
            case CONFLICT -> 409;
        };
        return ResponseEntity.status(status)
                .body(new CatalogDocuments.ApiError(problem.getKind().name(), problem.getMessage()));
    }
}
