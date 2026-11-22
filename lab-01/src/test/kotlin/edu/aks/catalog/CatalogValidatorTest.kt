package edu.aks.catalog

import edu.aks.catalog.model.CatalogProblem
import edu.aks.catalog.model.LessonSchedule
import edu.aks.catalog.service.CatalogValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatalogValidatorTest {
    private val validator = CatalogValidator()

    @Test
    fun `normalizes text before checking it`() {
        assertEquals("Курс", validator.courseTitle("   Курс   "))
        assertEquals("", validator.courseDescription(null))
        assertEquals("Тема", validator.lessonSchedule(LessonSchedule(1, " Тема ", 45)).topic)
    }

    @Test
    fun `rejects blank and oversized text`() {
        assertThrows<CatalogProblem> { validator.courseTitle(" \t ") }
        assertThrows<CatalogProblem> { validator.courseDescription("x".repeat(1001)) }
        assertThrows<CatalogProblem> { validator.lessonSchedule(LessonSchedule(1, "x".repeat(161), 1)) }
    }

    @Test
    fun `rejects invalid scheduling values`() {
        for (schedule in listOf(LessonSchedule(0, "Тема", 45), LessonSchedule(1, "Тема", 0),
            LessonSchedule(1, "Тема", 1441))) {
            assertEquals(CatalogProblem.Kind.INVALID,
                assertThrows<CatalogProblem> { validator.lessonSchedule(schedule) }.kind)
        }
    }
}
