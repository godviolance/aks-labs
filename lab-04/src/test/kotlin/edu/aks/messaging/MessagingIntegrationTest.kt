package edu.aks.messaging

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.sql.DriverManager
import java.time.Duration
import java.util.UUID

@EnabledIfSystemProperty(named = "catalogUrl", matches = ".+")
class MessagingIntegrationTest {
    private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
    private val json = JsonMapper.builder().build()
    private val base = URI.create(System.getProperty("catalogUrl"))

    @Test fun `audit all operations but notify only when a course is deleted`() {
        val title = "JMS " + UUID.randomUUID()
        val course = create("api/courses", """{"title":"$title","description":"original"}""")
        val courseId = course.substringAfterLast('/').toLong()
        val updatedTitle = "$title updated"
        val lesson = create("api/lessons", """{"courseId":$courseId,"topic":"JMS topic","minutes":45}""")
        val lessonId = lesson.substringAfterLast('/').toLong()
        assertEquals(204, send("PUT", lesson, """{"courseId":$courseId,"topic":"New topic","minutes":60}""").statusCode())
        assertEquals(409, send("DELETE", course).statusCode())
        assertEquals(204, send("PUT", course, """{"title":"$updatedTitle","description":"updated"}""").statusCode())
        assertEquals(204, send("DELETE", lesson).statusCode())
        assertEquals(204, send("DELETE", course).statusCode())

        val statuses = mutableMapOf<String, String>()
        await("six audit events") {
            connect().use { db ->
                db.prepareStatement("""
                    SELECT entity_type, operation, change_details::text FROM catalog_audit
                    WHERE (entity_type = 'COURSE' AND entity_id = ?)
                       OR (entity_type = 'LESSON' AND entity_id = ?)
                """.trimIndent()).use { query ->
                    query.setLong(1, courseId)
                    query.setLong(2, lessonId)
                    query.executeQuery().use { rows ->
                        statuses.clear()
                        while (rows.next()) {
                            val key = rows.getString(1) + "/" + rows.getString(2)
                            assertNull(statuses.put(key, rows.getString(3)), "Duplicate audit operation $key")
                        }
                        statuses.size == 6
                    }
                }
            }
        }
        assertEquals(setOf("COURSE/INSERT", "COURSE/UPDATE", "COURSE/DELETE",
            "LESSON/INSERT", "LESSON/UPDATE", "LESSON/DELETE"), statuses.keys)
        assertEquals(updatedTitle, json.readTree(statuses.getValue("COURSE/DELETE")).path("title").asString())
        assertEquals("60", json.readTree(statuses.getValue("LESSON/UPDATE")).path("minutes").asString())

        val mailbox = URI.create(System.getProperty("mailpitUrl")).resolve("api/v1/messages")
        await("exactly one email in Mailpit") {
            val response = http.send(HttpRequest.newBuilder(mailbox).GET().build(), BodyHandlers.ofString())
            val messages = json.readTree(response.body()).path("messages")
            val matching = messages.filter { it.path("Subject").asString().contains(title) }
            if (matching.isEmpty()) {
                false
            } else {
                assertEquals(1, matching.size)
                assertEquals("Удалён курс: $updatedTitle", matching.single().path("Subject").asString())
                assertTrue(matching.single().path("To").toString().contains("lab@example.invalid"))
                true
            }
        }
        assertEquals(404, send("GET", course).statusCode())
        assertEquals(404, send("GET", lesson).statusCode())
    }

    private fun connect() = DriverManager.getConnection(
        System.getenv("CATALOG_DB_URL") ?: "jdbc:postgresql://127.0.0.1:15432/catalog_jms",
        System.getenv("CATALOG_DB_USER") ?: "catalog", System.getenv("CATALOG_DB_PASSWORD"))

    private fun create(path: String, jsonBody: String): String {
        val response = send("POST", path, jsonBody)
        assertEquals(201, response.statusCode(), response.body())
        return response.headers().firstValue("location").orElseThrow()
    }

    private fun send(method: String, path: String, body: String? = null): java.net.http.HttpResponse<String> {
        val request = HttpRequest.newBuilder(base.resolve(path)).timeout(Duration.ofSeconds(10))
            .header("Accept", "application/json")
        if (body != null) request.header("Content-Type", "application/json")
        request.method(method, body?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody())
        return http.send(request.build(), BodyHandlers.ofString())
    }

    private fun await(label: String, ready: () -> Boolean) {
        val deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos()
        while (System.nanoTime() < deadline) {
            if (ready()) return
            Thread.sleep(100)
        }
        fail<Any>("Timed out waiting for $label")
    }
}
