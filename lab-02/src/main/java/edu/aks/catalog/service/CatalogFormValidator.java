package edu.aks.catalog.service;

import edu.aks.catalog.model.CatalogProblem;
import edu.aks.catalog.model.CatalogProblemKind;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

@Component
public class CatalogFormValidator {
    public int validateMinutes(String rawMinutes) {
        try {
            return Integer.parseInt(rawMinutes);
        } catch (NumberFormatException e) {
            throw new CatalogProblem(CatalogProblemKind.INVALID, "Укажите длительность целым числом.");
        }
    }

    public long validateId(String rawId) {
        try {
            long id = Long.parseLong(rawId);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Missing and malformed identifiers use the same error.
        }
        throw new CatalogProblem(CatalogProblemKind.INVALID, "Некорректный идентификатор.");
    }

    public boolean validateCsrfToken(String submittedToken, String sessionToken) {
        return StringUtils.isNotEmpty(submittedToken) && submittedToken.equals(sessionToken);
    }
}
