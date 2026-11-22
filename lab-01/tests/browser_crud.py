"""Run against a local lab instance. Only uniquely named test records are changed."""
import argparse
from uuid import uuid4

from playwright.sync_api import expect, sync_playwright


def exercise(page, base):
    prefix = "Проверка " + uuid4().hex[:8]
    first, second, renamed = (prefix + suffix for suffix in (" А", " Б", " В"))
    topic = prefix + " — занятие"

    def card(name):
        return page.get_by_role("article").filter(has=page.get_by_role("heading", name=name, exact=True))

    def form(scope, caption):
        scope.get_by_text(caption, exact=True).click()
        return scope.locator("details[open] form")

    def submit(target):
        target.get_by_role("button", name="Сохранить").click()

    page.goto(base)
    for title in (first, second):
        edit = form(page.locator("#courses"), "Добавить курс")
        edit.get_by_label("Название").fill(title)
        edit.get_by_label("Описание").fill("Тестовое описание")
        submit(edit)
        expect(card(title)).to_be_visible()

    edit = form(card(first), "Изменить курс")
    edit.get_by_label("Название").fill(renamed)
    submit(edit)
    expect(card(first)).to_have_count(0)
    expect(card(renamed)).to_contain_text("Тестовое описание")

    edit = form(page.locator("#lessons"), "Добавить занятие")
    edit.get_by_label("Тема").fill(topic)
    edit.get_by_label("Курс", exact=True).select_option(label=renamed)
    edit.get_by_label("Длительность, мин").fill("45")
    submit(edit)
    expect(card(topic)).to_contain_text(renamed)

    def post(data, status):
        response = page.request.post(base, form=data)
        assert response.status == status, (response.status, status, data.get("action"))
        return response.text()

    token = page.locator('input[name="token"]').first.input_value()
    lesson_id = card(topic).locator('input[name="id"]').first.input_value()
    course_id = card(renamed).locator('input[name="id"]').first.input_value()
    update = dict(token=token, action="lesson-update", id=lesson_id, courseId=course_id,
                  topic=topic + " изменено", minutes="0")
    assert "1440" in post(update, 400)
    post(dict(update, minutes="forty"), 400)
    post(dict(update, minutes="15", courseId="9223372036854775807"), 404)
    post(dict(update, minutes="15", id="0"), 400)
    post(dict(action="course-delete", id=course_id), 403)
    post(dict(token=token, action="course-update", id=course_id, title="  "), 400)

    card(renamed).get_by_role("button", name="Удалить курс").click()
    expect(page.get_by_role("alert")).to_contain_text("занятия")
    expect(card(renamed)).to_be_visible()
    expect(card(topic)).to_contain_text("45 мин")

    edit = form(card(topic), "Изменить занятие")
    edit.get_by_label("Тема").fill(topic + " II")
    edit.get_by_label("Длительность, мин").fill("60")
    edit.get_by_label("Курс", exact=True).select_option(label=second)
    submit(edit)
    page.reload()
    updated = card(topic + " II")
    expect(updated).to_contain_text(second)
    expect(updated).to_contain_text("60 мин")
    expect(card(topic)).to_have_count(0)
    card(renamed).get_by_role("button", name="Удалить курс").click()
    expect(card(renamed)).to_have_count(0)
    updated.get_by_role("button", name="Удалить занятие").click()
    expect(updated).to_have_count(0)
    card(second).get_by_role("button", name="Удалить курс").click()
    expect(card(second)).to_have_count(0)
    print("PASS: browser CRUD, lesson transfer, validation, missing course, protected delete and CSRF")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", default="http://127.0.0.1:8080/catalog/")
    parser.add_argument("--channel", default="msedge")
    args = parser.parse_args()
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(channel=args.channel)
        try:
            tab = browser.new_page()
            tab.set_default_timeout(5000)
            exercise(tab, args.url)
        finally:
            browser.close()
