package edu.aks.catalog.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LessonTest {
    @Test fun `rescheduling moves the lesson and updates its details together`() {
        val first = Course("First", "")
        val second = Course("Second", "")
        val lesson = Lesson(first, LessonSchedule(1, "Old topic", 40))
        lesson.reschedule(second, LessonSchedule(2, "New topic", 80))
        assertSame(second, lesson.course)
        assertEquals("New topic", lesson.topic)
        assertEquals(80, lesson.minutes)
    }
}
