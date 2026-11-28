package edu.aks.catalog.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CourseTest {
    @Test fun `revise title and description together`() {
        val course = Course("Initial", "")
        course.revise("Revised", "Description")
        assertEquals("Revised", course.title)
        assertEquals("Description", course.description)
    }
}
