# Практика 4 · JMS

Расширение приложения ПР3. Формы и REST используют один сервис каталога.
Вставка, изменение и удаление сохраняют событие в PostgreSQL в той же
транзакции, что и основное изменение. Планировщик передаёт сохранённые
события в Artemis как JMS `TextMessage` с JSON.

Для двух независимых потребителей настроены отдельные очереди.

| Очередь | Обработчик | Результат |
|---|---|---|
| `catalog.audit` | `CatalogAuditListener` | Записывает UUID, время, операцию, сущность, её ID и значения полей в `catalog_audit` |
| `catalog.notify` | `CatalogNotificationListener` | Через SMTP уведомляет об удалении курса |

Оба обработчика проверяют тип и структуру сообщения. Ключ `event_id`
защищает журнал от повторной обработки, а `catalog_notification`
отслеживает отправленные письма. Если процесс завершится после доставки
письма и до фиксации записи, SMTP может повторно доставить его.

## Запуск

Нужны JDK 25 и PostgreSQL. Для встроенного брокера закреплена версия
Artemis 2.44.0 с поддержкой JDK 25 и Netty 4.1 для файлового журнала брокера. [Примечания к выпуску](https://artemis.apache.org/components/artemis/download/release-notes-2.44.0).

Создайте пустую БД `catalog_jms`. Команды для PowerShell из корня проекта:

```powershell
psql -W -h 127.0.0.1 -p 15432 -U catalog -d catalog_jms -v ON_ERROR_STOP=1 -f lab-02/sql/catalog.sql
psql -W -h 127.0.0.1 -p 15432 -U catalog -d catalog_jms -v ON_ERROR_STOP=1 -f lab-04/sql/messaging.sql
docker run -d --name aks-lab04-mailpit -p 127.0.0.1:18025:8025 -p 127.0.0.1:11025:1025 axllent/mailpit:v1.28.4
.\lab-04\gradlew.bat -p lab-04 clean build
$env:CATALOG_DB_PASSWORD = Read-Host 'Пароль пользователя PostgreSQL' -MaskInput
java '-Djava.net.preferIPv4Stack=true' -jar lab-04/build/libs/catalog-jms.jar
```

Подключение к БД настраивается через `CATALOG_DB_URL`,
`CATALOG_DB_USER` и `CATALOG_DB_PASSWORD`. Для почты доступны
`CATALOG_SMTP_HOST`, `CATALOG_SMTP_PORT`, `CATALOG_MAIL_SENDER`
и `CATALOG_MAIL_RECIPIENTS` (адреса через запятую). По умолчанию почта
поступает в локальный Mailpit для `lab@example.invalid`. Параметр Java
в примере включает IPv4 для опубликованного SMTP-порта Docker Desktop.

- [Каталог](http://127.0.0.1:8083/catalog/)
- [REST API](http://127.0.0.1:8083/catalog/api/courses)
- [Локальный Mailpit](http://127.0.0.1:18025/)

## Проверка

При запущенном приложении, пустых очередях и тестовом получателе по
умолчанию интеграционный тест выполняет шесть изменений, сверяет их с
аудитом и проверяет одно уведомление в Mailpit. Используйте ту же
`CATALOG_DB_PASSWORD`, что при запуске.

```powershell
.\lab-04\gradlew.bat -p lab-04 test "-PcatalogUrl=http://127.0.0.1:8083/catalog/" "-PmailpitUrl=http://127.0.0.1:18025/"
```
