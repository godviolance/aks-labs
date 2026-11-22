BEGIN;

CREATE TABLE course (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title varchar(120) NOT NULL CHECK (btrim(title) <> ''),
    description varchar(1000) NOT NULL DEFAULT ''
);

CREATE TABLE lesson (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id bigint NOT NULL REFERENCES course(id) ON DELETE RESTRICT,
    topic varchar(160) NOT NULL CHECK (btrim(topic) <> ''),
    minutes integer NOT NULL CHECK (minutes BETWEEN 1 AND 1440)
);

CREATE INDEX lesson_course_idx ON lesson(course_id);

WITH new_course AS (
    INSERT INTO course (title, description)
    VALUES ('Реляционные базы данных', 'Таблицы, связи и запросы.')
    RETURNING id
)
INSERT INTO lesson (course_id, topic, minutes)
SELECT id, topic, minutes FROM new_course
CROSS JOIN (VALUES ('Связи между таблицами', 45), ('Первый JOIN', 60)) AS topics(topic, minutes);

INSERT INTO course (title, description)
VALUES ('Основы Java', 'Работа с объектами и коллекциями.');

COMMIT;
