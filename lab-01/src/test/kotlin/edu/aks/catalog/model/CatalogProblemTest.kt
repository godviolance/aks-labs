package edu.aks.catalog.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogProblemTest {
    @Test fun `expose both the failure category and its message`() {
        val problem = CatalogProblem(CatalogProblemKind.MISSING, "Курс не найден.")
        assertEquals(CatalogProblemKind.MISSING, problem.kind)
        assertEquals("Курс не найден.", problem.message)
    }
}
