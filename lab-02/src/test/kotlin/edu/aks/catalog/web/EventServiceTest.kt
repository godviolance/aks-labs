package edu.aks.catalog.web

import edu.aks.catalog.model.CatalogProblem
import edu.aks.catalog.model.LessonSchedule
import edu.aks.catalog.service.CatalogService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EventServiceTest {
    private class RecordingCatalog : CatalogService() {
        val calls = mutableListOf<List<Any>>()
        override fun addCourse(title: String, description: String): Long {
            calls += listOf("addCourse", title, description)
            return 1
        }
        override fun updateCourse(id: Long, title: String, description: String) {
            calls += listOf("updateCourse", id, title, description)
        }
        override fun deleteCourse(id: Long) { calls += listOf("deleteCourse", id) }
        override fun addLesson(input: LessonSchedule): Long {
            calls += listOf("addLesson", input)
            return 1
        }
        override fun updateLesson(id: Long, input: LessonSchedule) { calls += listOf("updateLesson", id, input) }
        override fun deleteLesson(id: Long) { calls += listOf("deleteLesson", id) }
    }

    private val catalog = RecordingCatalog()
    private val service = EventService(catalog, CatalogFormValidator())
    private val input = CatalogEventInput("13", "Курс", "Описание", "21", "Тема", "45")

    @Test fun `route all six event types with the relevant arguments`() {
        CatalogEvent.entries.forEach { service.handleEvent(it, input) }
        val schedule = LessonSchedule(21, "Тема", 45)
        assertEquals(listOf(
            listOf("addCourse", "Курс", "Описание"),
            listOf("updateCourse", 13L, "Курс", "Описание"),
            listOf("deleteCourse", 13L),
            listOf("addLesson", schedule),
            listOf("updateLesson", 13L, schedule),
            listOf("deleteLesson", 13L)
        ), catalog.calls)
    }

    @Test fun `reject invalid values before calling the catalog`() {
        val invalid = CatalogEventInput("x", null, null, "21", "Тема", "x")
        assertThrows<CatalogProblem> { service.handleEvent(CatalogEvent.LESSON_CREATE, invalid) }
        assertThrows<CatalogProblem> { service.handleEvent(CatalogEvent.COURSE_DELETE, invalid) }
        assertEquals(emptyList<Any>(), catalog.calls)
    }
}
