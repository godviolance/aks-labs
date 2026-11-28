# Практика 2 · Spring

Каталог курсов и занятий из первой практики. В интерфейсе можно создать,
просмотреть, изменить и удалить курсы и занятия и перенести занятие в другой
курс. Удаление курса с занятиями возвращает конфликт.

- Данные — сущности `Course` и `Lesson`, Hibernate и PostgreSQL. Схему и
  демонстрационные данные создаёт `sql/catalog.sql`.
- Бизнес-логика — Spring Beans `CatalogService`, `CatalogValidator` и
  `CatalogRepository`. Изменения выполняются в транзакциях Spring.
- Представление — Spring MVC, `CatalogController` и шаблоны Thymeleaf.
  `EventService` разбирает операции формы, а `CatalogFormValidator` проверяет
  параметры и CSRF-токен.

## Запуск

Нужны JDK 25 и доступная PostgreSQL. Создайте отдельную пустую базу
`catalog_spring`, доступную пользователю `catalog`. Пример для PowerShell
из корня репозитория.

```powershell
psql -W -h 127.0.0.1 -p 15432 -U catalog -d catalog_spring -v ON_ERROR_STOP=1 -f lab-02/sql/catalog.sql
.\lab-02\gradlew.bat -p lab-02 clean build
$env:CATALOG_DB_PASSWORD = Read-Host 'Пароль пользователя PostgreSQL' -MaskInput
java -jar lab-02/build/libs/catalog-spring.jar
```

Параметры подключения настраиваются переменными `CATALOG_DB_URL`,
`CATALOG_DB_USER` и `CATALOG_DB_PASSWORD`. Каталог открывается на
[127.0.0.1:8081/catalog](http://127.0.0.1:8081/catalog/).
На Windows при проблеме с загрузкой `GradleWorkerMain` используйте
копию проекта и `GRADLE_USER_HOME` с ASCII-путями.

## Проверка

Модульные тесты запускаются командой `clean build`. Отчёт JaCoCo доступен
в `build/reports/jacoco/test/html/index.html`. При запущенном приложении и
установленном Edge можно прогнать интеграционный тест полного CRUD:

```powershell
.\lab-02\gradlew.bat -p lab-02 test "-PcatalogUrl=http://127.0.0.1:8081/catalog/"
```
