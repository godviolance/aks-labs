package edu.aks.catalog.web

import edu.aks.catalog.model.CatalogProblem
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatalogEventTest {
    @Test fun `map each existing form action`() {
        val actions = listOf("course-create", "course-update", "course-delete",
            "lesson-create", "lesson-update", "lesson-delete")
        assertArrayEquals(CatalogEvent.entries.toTypedArray(), actions.map(CatalogEvent::parseAction).toTypedArray())
    }

    @Test fun `reject missing or unknown action`() {
        for (action in listOf(null, "", "other")) {
            assertThrows<CatalogProblem> { CatalogEvent.parseAction(action) }
        }
    }
}
