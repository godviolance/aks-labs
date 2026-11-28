package edu.aks.rest.api

import edu.aks.catalog.model.CatalogProblem
import edu.aks.catalog.model.CatalogProblemKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogApiErrorsTest {
    @Test fun `preserve problem details and map statuses`() {
        val errors = CatalogApiErrors()
        for ((kind, status) in mapOf(CatalogProblemKind.INVALID to 400,
            CatalogProblemKind.MISSING to 404, CatalogProblemKind.CONFLICT to 409)) {
            val response = errors.handleCatalogProblem(CatalogProblem(kind, "Ошибка"))
            assertEquals(status, response.statusCode.value())
            assertEquals(CatalogDocuments.ApiError(kind.name, "Ошибка"), response.body)
        }
    }
}
