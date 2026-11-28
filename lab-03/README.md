# Практика 3 · REST

Решение продолжает Spring-приложение из ПР2. JAX-RS подходит для каталога
на GlassFish из ПР1. Spring REST выбран для этой работы, потому что он
переиспользует существующие Spring Beans, Hibernate, транзакции и
валидацию ПР2. Gradle подключает `lab-02` как составную сборку.

| Метод | Путь от `/catalog/api` | Результат |
|---|---|---|
| GET | `/courses`, `/lessons` | Списки, занятия можно отфильтровать по `courseId` |
| GET | `/courses/{id}`, `/lessons/{id}` | Одна запись |
| POST | `/courses`, `/lessons` | 201 и `Location` |
| PUT | `/courses/{id}`, `/lessons/{id}` | 204 |
| DELETE | `/courses/{id}`, `/lessons/{id}` | 204 |

Курс принимает `title` и `description`. Занятие принимает `courseId`,
`topic` и `minutes`. Указывайте `Content-Type` для POST и PUT и `Accept`
для представления ответа. Поддерживаются `application/json` и
`application/xml`. Ошибки каталога возвращают 400, 404 или 409.

XML содержит инструкцию `xml-stylesheet`. Статический `catalog.xsl`
отображает списки, карточки и переходы между занятиями и курсами.
`?format=xml` открывает XML в браузере. В браузерах без клиентского XSLT
откройте `?format=html`, чтобы увидеть HTML, полученный тем же XSL на сервере.
[Сведения об отключении XSLT в Chromium](https://developer.chrome.com/docs/web-platform/deprecating-xslt).

## Запуск и проверка

Создайте отдельную пустую базу `catalog_rest`. Выполните команды из корня
репозитория под JDK 25. Для Windows используйте ASCII-пути при сборке.

```powershell
psql -W -h 127.0.0.1 -p 15432 -U catalog -d catalog_rest -v ON_ERROR_STOP=1 -f lab-02/sql/catalog.sql
.\lab-03\gradlew.bat -p lab-03 clean build
$env:CATALOG_DB_PASSWORD = Read-Host 'Пароль пользователя PostgreSQL' -MaskInput
java -jar lab-03/build/libs/catalog-rest.jar
```

```powershell
curl.exe -H 'Accept:application/xml' http://127.0.0.1:8082/catalog/api/courses
curl.exe -H 'Accept:application/json' http://127.0.0.1:8082/catalog/api/lessons
.\lab-03\gradlew.bat -p lab-03 test "-PcatalogUrl=http://127.0.0.1:8082/catalog/"
```

[HTML-представление каталога](http://127.0.0.1:8082/catalog/api/courses?format=html)
применяет XSL на сервере. [Формы ПР2](http://127.0.0.1:8082/catalog/) также
работают в этом приложении.
