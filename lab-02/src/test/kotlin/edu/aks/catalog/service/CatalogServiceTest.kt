package edu.aks.catalog.service

import edu.aks.catalog.injectTestField
import edu.aks.catalog.model.*
import edu.aks.catalog.repository.CatalogRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class CatalogServiceTest {
    private val course = Course("Existing", "")
    private var hasLessons = false
    private var deleted = false
    private var inserted: Lesson? = null
    private val service = CatalogService().apply {
        injectTestField(this, "validator", CatalogValidator())
        injectTestField(this, "repository", object : CatalogRepository() {
            override fun findCourseForUpdate(id: Long): Optional<Course> =
                Optional.ofNullable(course.takeIf { id == 1L })

            override fun findLessonForUpdate(id: Long): Optional<Lesson> = Optional.empty()
            override fun existsLessonForCourse(id: Long) = hasLessons
            override fun deleteCourse(course: Course) { deleted = true }
            override fun insertLesson(lesson: Lesson) { inserted = lesson }
        })
    }

    @Test fun `reject missing courses and lessons separately`() {
        val courseProblem = assertThrows<CatalogProblem> { service.deleteCourse(2) }
        val lessonProblem = assertThrows<CatalogProblem> { service.deleteLesson(2) }
        assertEquals(CatalogProblemKind.MISSING, courseProblem.kind)
        assertEquals(CatalogProblemKind.MISSING, lessonProblem.kind)
        assertNotEquals(courseProblem.message, lessonProblem.message)
    }

    @Test fun `refuse to delete a course with lessons`() {
        hasLessons = true
        assertEquals(CatalogProblemKind.CONFLICT, assertThrows<CatalogProblem> { service.deleteCourse(1) }.kind)
        assertFalse(deleted)
        hasLessons = false
        service.deleteCourse(1)
        assertTrue(deleted)
    }

    @Test fun `validate a schedule before persistence`() {
        assertThrows<CatalogProblem> { service.addLesson(LessonSchedule(1, "Topic", 0)) }
        assertNull(inserted)
        service.addLesson(LessonSchedule(1, " Topic ", 45))
        assertEquals("Topic", inserted!!.topic)
        assertSame(course, inserted!!.course)
    }
}
