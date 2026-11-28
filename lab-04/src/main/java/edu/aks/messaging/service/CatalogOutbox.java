package edu.aks.messaging.service;

import edu.aks.catalog.model.CatalogChange;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogOutbox {
    public static final String AUDIT_QUEUE = "catalog.audit";
    public static final String NOTIFY_QUEUE = "catalog.notify";

    private final JdbcTemplate db;
    private final JmsTemplate jms;
    private final CatalogMessageCodec codec;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void storeChange(CatalogChange change) {
        db.update("INSERT INTO catalog_outbox(event_id, occurred_at, payload) VALUES (?, ?, ?)",
                change.eventId(), OffsetDateTime.ofInstant(change.occurredAt(), ZoneOffset.UTC),
                codec.encodeChange(change));
    }

    @Scheduled(fixedDelayString = "${catalog.outbox.delay:500}")
    @Transactional
    public void publishPendingChanges() {
        var pending = db.query("""
                SELECT event_id, payload FROM catalog_outbox
                ORDER BY occurred_at, event_id LIMIT 20 FOR UPDATE SKIP LOCKED
                """, (row, index) -> new OutboxEntry(row.getObject("event_id", UUID.class), row.getString("payload")));
        for (OutboxEntry entry : pending) {
            sendChange(AUDIT_QUEUE, entry);
            sendChange(NOTIFY_QUEUE, entry);
            db.update("DELETE FROM catalog_outbox WHERE event_id = ?", entry.id());
        }
    }

    private void sendChange(String destination, OutboxEntry entry) {
        jms.send(destination, session -> {
            var message = session.createTextMessage(entry.payload());
            message.setStringProperty("_AMQ_DUPL_ID", entry.id().toString());
            return message;
        });
    }

    private record OutboxEntry(UUID id, String payload) {}
}
