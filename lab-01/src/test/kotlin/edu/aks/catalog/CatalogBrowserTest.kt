package edu.aks.catalog

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.FormData
import com.microsoft.playwright.options.RequestOptions
import com.microsoft.playwright.options.SelectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.UUID

@EnabledIfSystemProperty(named = "catalog.url", matches = ".+")
class CatalogBrowserTest {
    @Test
    fun `course and lesson CRUD through the browser`() {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setChannel("msedge"))
            browser.newPage().use { page ->
                page.setDefaultTimeout(5000.0)
                exercise(page, System.getProperty("catalog.url"))
            }
        }
    }

    private fun exercise(page: Page, url: String) {
        val prefix = "Проверка " + UUID.randomUUID().toString().take(8)
        val first = "$prefix А"
        val second = "$prefix Б"
        val renamed = "$prefix В"
        val topic = "$prefix — занятие"
        fun card(name: String): Locator = page.getByRole(AriaRole.ARTICLE).filter(
            Locator.FilterOptions().setHas(page.getByRole(AriaRole.HEADING,
                Page.GetByRoleOptions().setName(name).setExact(true)))
        )

        page.navigate(url)
        for (title in listOf(first, second)) {
            val editor = form(page.locator("#courses"), "Добавить курс")
            editor.getByLabel("Название").fill(title)
            editor.getByLabel("Описание").fill("Тестовое описание")
            save(editor)
            assertThat(card(title)).isVisible()
        }
        val course = form(card(first), "Изменить курс")
        course.getByLabel("Название").fill(renamed)
        save(course)
        assertThat(card(first)).hasCount(0)
        assertThat(card(renamed)).containsText("Тестовое описание")

        val lesson = form(page.locator("#lessons"), "Добавить занятие")
        lesson.getByLabel("Тема").fill(topic)
        chooseCourse(lesson, renamed)
        lesson.getByLabel("Длительность, мин").fill("45")
        save(lesson)
        assertThat(card(topic)).containsText(renamed)

        val token = page.locator("input[name=token]").first().inputValue()
        val lessonId = card(topic).locator("input[name=id]").first().inputValue()
        val courseId = card(renamed).locator("input[name=id]").first().inputValue()
        fun post(status: Int, vararg fields: Pair<String, String>) {
            val data = FormData.create()
            fields.forEach { (key, value) -> data.set(key, value) }
            page.request().post(url, RequestOptions.create().setMaxRedirects(0).setForm(data)).also {
                assertEquals(status, it.status())
                if (status == 303) assertEquals(page.url().let { java.net.URI(it).path }, it.headers()["location"])
                it.dispose()
            }
        }
        fun updateLesson(status: Int, minutes: String, target: String = courseId, id: String = lessonId) {
            post(status, "token" to token, "action" to "lesson-update", "id" to id,
                "courseId" to target, "topic" to "$topic изменено", "minutes" to minutes)
        }
        updateLesson(400, "0")
        updateLesson(400, "forty")
        updateLesson(404, "15", Long.MAX_VALUE.toString())
        updateLesson(400, "15", id = "0")
        updateLesson(404, "15", id = Long.MAX_VALUE.toString())
        post(400, "token" to token, "action" to "unknown")
        post(403, "token" to "wrong", "action" to "course-delete", "id" to courseId)
        post(409, "token" to token, "action" to "course-delete", "id" to courseId)
        post(303, "token" to token, "action" to "course-update", "id" to courseId,
            "title" to renamed, "description" to "Тестовое описание")
        post(403, "action" to "course-delete", "id" to courseId)
        post(400, "token" to token, "action" to "course-update", "id" to courseId, "title" to "  ")
        delete(card(renamed), "Удалить курс")
        assertThat(page.getByRole(AriaRole.ALERT)).containsText("занятия")
        assertThat(card(renamed)).isVisible()
        assertThat(card(topic)).containsText("45 мин")

        val change = form(card(topic), "Изменить занятие")
        change.getByLabel("Тема").fill("$topic II")
        change.getByLabel("Длительность, мин").fill("60")
        chooseCourse(change, second)
        save(change)
        page.reload()
        val updated = card("$topic II")
        assertThat(updated).containsText(second)
        assertThat(updated).containsText("60 мин")
        assertThat(card(topic)).hasCount(0)
        delete(card(renamed), "Удалить курс")
        assertThat(card(renamed)).hasCount(0)
        delete(updated, "Удалить занятие")
        assertThat(updated).hasCount(0)
        delete(card(second), "Удалить курс")
        assertThat(card(second)).hasCount(0)
    }

    private fun form(scope: Locator, caption: String): Locator {
        scope.getByText(caption, Locator.GetByTextOptions().setExact(true)).click()
        return scope.locator("details[open] form")
    }

    private fun chooseCourse(form: Locator, title: String) {
        form.getByLabel("Курс", Locator.GetByLabelOptions().setExact(true))
            .selectOption(SelectOption().setLabel(title))
    }

    private fun save(form: Locator) = clickButton(form, "Сохранить")
    private fun delete(card: Locator, label: String) = clickButton(card, label)
    private fun clickButton(scope: Locator, label: String) {
        scope.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName(label)).click()
    }
}
