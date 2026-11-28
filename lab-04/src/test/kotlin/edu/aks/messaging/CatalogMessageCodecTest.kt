package edu.aks.messaging

import edu.aks.catalog.model.CatalogChange
import edu.aks.catalog.model.ChangeOperation
import edu.aks.catalog.model.EntityType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CatalogMessageCodecTest {
    private val codec = CatalogMessageCodec()
    private val change = CatalogChange(UUID.randomUUID(), Instant.parse("2026-11-28T08:00:00Z"),
        ChangeOperation.DELETE, EntityType.COURSE, 2, mapOf("title" to "Курс", "description" to ""))

    @Test fun `round trip the complete change`() {
        assertEquals(change, codec.decodeChange(codec.encodeChange(change)).orElseThrow())
    }

    @Test fun `reject malformed events and incomplete details`() {
        for (input in listOf(null, "not json", "null", "{}",
            codec.encodeChange(CatalogChange(change.eventId(), change.occurredAt(), ChangeOperation.INSERT,
                EntityType.LESSON, 2, mapOf("topic" to "missing course and duration"))))) {
            assertTrue(codec.decodeChange(input).isEmpty)
        }
    }
}
