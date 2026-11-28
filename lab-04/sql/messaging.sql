BEGIN;

CREATE TABLE catalog_outbox (
    event_id uuid PRIMARY KEY,
    occurred_at timestamptz NOT NULL,
    payload text NOT NULL
);

CREATE TABLE catalog_audit (
    event_id uuid PRIMARY KEY,
    occurred_at timestamptz NOT NULL,
    operation varchar(10) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    entity_type varchar(10) NOT NULL CHECK (entity_type IN ('COURSE', 'LESSON')),
    entity_id bigint NOT NULL CHECK (entity_id > 0),
    change_details jsonb NOT NULL
);

CREATE TABLE catalog_notification (
    event_id uuid PRIMARY KEY,
    sent_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMIT;
