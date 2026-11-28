package edu.aks.catalog.web

import edu.aks.catalog.model.CatalogProblem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatalogFormValidatorTest {
    private val validator = CatalogFormValidator()

    @Test fun `parse integer minutes`() {
        assertEquals(45, validator.validateMinutes("45"))
        for (raw in listOf(null, "", "4.5", "2147483648")) {
            assertThrows<CatalogProblem> { validator.validateMinutes(raw) }
        }
    }

    @Test fun `accept only positive numeric IDs`() {
        assertEquals(42L, validator.validateId("42"))
        for (raw in listOf(null, "", "0", "-1", "abc")) {
            assertThrows<CatalogProblem> { validator.validateId(raw) }
        }
    }

    @Test fun `match both nonempty tokens`() {
        assertTrue(validator.validateCsrfToken("expected", "expected"))
        assertFalse(validator.validateCsrfToken("other", "expected"))
        assertFalse(validator.validateCsrfToken("expected", null))
        assertFalse(validator.validateCsrfToken(null, "expected"))
        assertFalse(validator.validateCsrfToken("", ""))
    }
}
