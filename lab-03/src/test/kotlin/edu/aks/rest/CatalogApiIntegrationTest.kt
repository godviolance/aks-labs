package edu.aks.rest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.UUID

@EnabledIfSystemProperty(named = "catalog.url", matches = ".+")
class CatalogApiIntegrationTest {
    private val base = URI.create(System.getProperty("catalog.url"))
    private val client = HttpClient.newHttpClient()

    @Test fun `perform CRUD in JSON and XML and navigate through the stylesheet`() {
        val marker = "REST " + UUID.randomUUID()
        val start = send("GET", "api/courses", "application/xml")
        assertEquals(200, start.statusCode())
        assertTrue(start.body().startsWith("<?xml-stylesheet"))
        assertTrue(start.body().contains("/catalog/catalog.xsl"))
        assertTrue(start.body().contains("<courses>"))
        assertTrue(send("GET", "catalog.xsl", "application/xml").body().contains("xsl:stylesheet"))
        val html = send("GET", "api/courses?format=html", "text/html")
        assertEquals(200, html.statusCode())
        assertTrue(html.body().contains("<html"))
        assertTrue(html.body().contains("format=html"))
        assertTrue(send("GET", "api/lessons", "application/json").body().contains("\"lessons\""))

        val course = send("POST", "api/courses", "application/json", """{"title":"$marker","description":"test"}""")
        assertEquals(201, course.statusCode())
        val coursePath = course.headers().firstValue("location").orElseThrow()
        val courseId = coursePath.substringAfterLast('/').toLong()
        var lessonPath: String? = null
        try {
            assertTrue(send("GET", coursePath, "application/xml").body().contains(marker))
            val changed = send("PUT", coursePath, "application/xml",
                "<course><title>$marker updated</title><description>XML</description></course>")
            assertEquals(204, changed.statusCode())
            assertTrue(send("GET", coursePath, "application/json").body().contains("$marker updated"))
            val lesson = send("POST", "api/lessons", "application/xml",
                "<lesson><courseId>$courseId</courseId><topic>Integration</topic><minutes>45</minutes></lesson>")
            assertEquals(201, lesson.statusCode())
            lessonPath = lesson.headers().firstValue("location").orElseThrow()
            val current = send("GET", lessonPath, "application/xml")
            assertTrue(current.body().contains("<courseId>$courseId</courseId>"))
            assertEquals(204, send("PUT", lessonPath, "application/json",
                """{"courseId":$courseId,"topic":"Updated","minutes":60}""").statusCode())
            assertTrue(send("GET", lessonPath, "application/json").body().contains("Updated"))
            assertEquals(409, send("DELETE", coursePath, "application/json").statusCode())
        } finally {
            lessonPath?.let {
                assertEquals(204, send("DELETE", it, "application/xml").statusCode())
                assertEquals(404, send("GET", it, "application/xml").statusCode())
            }
            assertEquals(204, send("DELETE", coursePath, "application/json").statusCode())
        }
        assertEquals(404, send("GET", coursePath, "application/json").statusCode())
        assertEquals(400, send("POST", "api/courses", "application/json", """{"title":"  "}""").statusCode())
    }

    private fun send(method: String, path: String, mime: String, body: String? = null): java.net.http.HttpResponse<String> {
        val request = HttpRequest.newBuilder(base.resolve(path)).header("Accept", mime)
        if (body != null) request.header("Content-Type", mime)
        request.method(method, body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody())
        return client.send(request.build(), BodyHandlers.ofString())
    }
}
