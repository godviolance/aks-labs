package edu.aks.catalog.service

import edu.aks.catalog.model.CatalogProblem
import edu.aks.catalog.model.CatalogProblemKind
import edu.aks.catalog.model.LessonSchedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatalogValidatorTest {
    private val validator = CatalogValidator()

    @Test
    fun `normalizes text before checking it`() {
        assertEquals("Курс", validator.validateCourseTitle("   Курс   "))
        assertEquals("", validator.validateCourseDescription(null))
        assertEquals("Тема", validator.validateLessonSchedule(LessonSchedule(1, " Тема ", 45)).topic())
    }

    @Test
    fun `rejects blank and oversized text`() {
        assertThrows<CatalogProblem> { validator.validateCourseTitle(" \t ") }
        assertThrows<CatalogProblem> { validator.validateCourseDescription("x".repeat(1001)) }
        assertThrows<CatalogProblem> { validator.validateLessonSchedule(LessonSchedule(1, "x".repeat(161), 1)) }
    }

    @Test
    fun `rejects invalid scheduling values`() {
        for (schedule in listOf(LessonSchedule(0, "Тема", 45), LessonSchedule(1, "Тема", 0),
            LessonSchedule(1, "Тема", 1441))) {
            assertEquals(CatalogProblemKind.INVALID,
                assertThrows<CatalogProblem> { validator.validateLessonSchedule(schedule) }.kind)
        }
    }
}
