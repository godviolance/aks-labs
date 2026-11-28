package edu.aks.catalog.web

import edu.aks.catalog.injectTestField
import edu.aks.catalog.stubInterface
import edu.aks.catalog.service.CatalogService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogServletTest {
    @Test fun `delegate GET and POST once each`() {
        val catalog = CatalogService()
        val validator = CatalogFormValidator()
        val calls = mutableListOf<String>()
        val web = object : CatalogWebService(catalog, EventService(catalog, validator), validator) {
            override fun showCatalog(req: HttpServletRequest, res: HttpServletResponse) { calls += "show" }
            override fun submitCatalogForm(req: HttpServletRequest, res: HttpServletResponse) { calls += "submit" }
        }
        val servlet = CatalogServlet()
        injectTestField(servlet, "webService", web)
        val res = stubInterface<HttpServletResponse> { name, _ -> error("Unexpected response call: $name") }
        for (verb in listOf("GET", "POST")) {
            val req = stubInterface<HttpServletRequest> { name, _ ->
                when (name) {
                    "getMethod" -> verb
                    "getLastModified" -> -1L
                    else -> error("Unexpected request call: $name")
                }
            }
            // Invoke the servlet's public Servlet entry point, not its protected overload.
            servlet.service(req as jakarta.servlet.ServletRequest, res as jakarta.servlet.ServletResponse)
        }
        assertEquals(listOf("show", "submit"), calls)
    }
}
