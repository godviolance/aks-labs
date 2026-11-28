package edu.aks.catalog.repository

import edu.aks.catalog.model.Course
import edu.aks.catalog.model.Lesson
import edu.aks.catalog.stubInterface
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CatalogRepositoryTest {
    @Test fun `lock an existing course and represent a missing lesson as empty`() {
        val course = Course("Курс", "")
        val calls = mutableListOf<List<Any?>>()
        val em = stubInterface<EntityManager> { method, args ->
            assertEquals("find", method)
            calls.add(args)
            if (args.first() == Course::class.java) course else null
        }
        val repository = CatalogRepository(em)
        assertSame(course, repository.findCourseForUpdate(12).orElseThrow())
        assertTrue(repository.findLessonForUpdate(13).isEmpty)
        assertEquals(listOf(
            listOf(Course::class.java, 12L, LockModeType.PESSIMISTIC_WRITE),
            listOf(Lesson::class.java, 13L, LockModeType.PESSIMISTIC_WRITE)
        ), calls)
    }
}
