package edu.aks.catalog.web

import edu.aks.catalog.model.CatalogProblem
import edu.aks.catalog.model.CatalogProblemKind
import edu.aks.catalog.service.CatalogService
import edu.aks.catalog.stubInterface
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CatalogWebServiceTest {
    private val catalog = object : CatalogService() {
        override fun listCourses() = emptyList<edu.aks.catalog.model.Course>()
        override fun listLessons() = emptyList<edu.aks.catalog.model.Lesson>()
    }
    private val validator = CatalogFormValidator()
    private val tokens = mutableMapOf<String, Any>()
    private val session = stubInterface<HttpSession> { method, args ->
        when (method) {
            "getAttribute" -> tokens[args[0]]
            "setAttribute" -> { tokens[args[0] as String] = args[1]!!; null }
            else -> error(method)
        }
    }
    private val attributes = mutableMapOf<String, Any>()
    private val headers = mutableMapOf<String, String>()
    private var status = 200
    private var forwards = 0
    private var eventCalls = 0
    private var problem: CatalogProblemKind? = null
    private val response = stubInterface<HttpServletResponse> { method, args ->
        when (method) {
            "setStatus", "sendError" -> status = args[0] as Int
            "setHeader" -> headers[args[0] as String] = args[1] as String
            else -> error(method)
        }
        null
    }
    private val web = CatalogWebService(catalog, object : EventService(catalog, validator) {
        override fun handleEvent(event: CatalogEvent, input: CatalogEventInput) {
            assertEquals(CatalogEvent.COURSE_CREATE, event)
            assertEquals("Курс", input.title())
            eventCalls++
            problem?.let { throw CatalogProblem(it, "сообщение") }
        }
    }, validator)

    private fun request(token: String?, action: String = "course-create") =
        stubInterface<HttpServletRequest> { method, args ->
            when (method) {
                "getSession" -> session
                "getParameter" -> mapOf("token" to token, "action" to action, "title" to "Курс")[args[0]]
                "getContextPath" -> "/catalog"
                "setCharacterEncoding" -> { assertEquals("UTF-8", args[0]); null }
                "setAttribute" -> { attributes[args[0] as String] = args[1]!!; null }
                "getRequestDispatcher" -> {
                    assertEquals("/WEB-INF/catalog.jsp", args[0])
                    stubInterface<RequestDispatcher> { name, _ -> assertEquals("forward", name); forwards++; null }
                }
                else -> error(method)
            }
        }

    @Test fun `prepare catalog view and preserve the session token`() {
        web.showCatalog(request(null), response)
        val token = tokens["token"]
        assertNotNull(token)
        web.showCatalog(request(null), response)
        assertEquals(token, tokens["token"])
        assertEquals("no-store", headers["Cache-Control"])
        assertEquals(emptyList<Any>(), attributes["courses"])
        assertEquals(emptyList<Any>(), attributes["lessons"])
        assertEquals(2, forwards)
    }

    @Test fun `forbid an invalid token without dispatching the event`() {
        tokens["token"] = "right"
        web.submitCatalogForm(request("wrong"), response)
        assertEquals(403, status)
        assertEquals(0, eventCalls)
    }

    @Test fun `redirect a successful form submission`() {
        tokens["token"] = "right"
        web.submitCatalogForm(request("right"), response)
        assertEquals(303, status)
        assertEquals("/catalog/", headers["Location"])
        assertEquals(1, eventCalls)
    }

    @Test fun `render failures with status and message`() {
        tokens["token"] = "right"
        for ((kind, code) in mapOf(CatalogProblemKind.INVALID to 400, CatalogProblemKind.MISSING to 404,
            CatalogProblemKind.CONFLICT to 409)) {
            problem = kind
            web.submitCatalogForm(request("right"), response)
            assertEquals(code, status)
            assertEquals("сообщение", attributes["error"])
        }
        assertEquals(3, forwards)
    }
}
