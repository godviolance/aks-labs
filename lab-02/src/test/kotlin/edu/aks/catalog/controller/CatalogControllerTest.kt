package edu.aks.catalog.controller

import edu.aks.catalog.stubInterface
import edu.aks.catalog.repository.CatalogRepository
import edu.aks.catalog.service.CatalogValidator
import jakarta.persistence.EntityManager

import edu.aks.catalog.model.Course
import edu.aks.catalog.model.Lesson
import edu.aks.catalog.service.CatalogService
import edu.aks.catalog.service.CatalogFormValidator
import edu.aks.catalog.service.EventService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession

class CatalogControllerTest {
    private val catalog = object : CatalogService(
        CatalogRepository(stubInterface<EntityManager> { name, _ -> error(name) }), CatalogValidator(), { }
    ) {
        override fun listCourses() = emptyList<Course>()
        override fun listLessons() = emptyList<Lesson>()
    }
    private val validator = CatalogFormValidator()
    private val controller = CatalogController(catalog, EventService(catalog, validator), validator)

    @Test fun `prepare catalog and issue the session token`() {
        val session = MockHttpSession()
        val response = MockHttpServletResponse()
        val view = controller.showCatalog(session, response)
        assertEquals("no-store", response.getHeader("Cache-Control"))
        assertEquals("catalog", view.viewName)
        assertEquals(emptyList<Course>(), view.model["courses"])
        assertNotNull(session.getAttribute("token"))
    }

    @Test fun `reject unauthorized submission without creating a session`() {
        val request = MockHttpServletRequest()
        val result = controller.submitCatalogForm(emptyMap(), request)
        assertEquals(HttpStatus.FORBIDDEN, result.status)
        assertNull(request.getSession(false))
    }
}
