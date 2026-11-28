package edu.aks.messaging.listener;

import edu.aks.catalog.model.CatalogChange;
import lombok.RequiredArgsConstructor;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import edu.aks.messaging.service.CatalogMessageCodec;
import edu.aks.messaging.service.CatalogOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
@RequiredArgsConstructor
public class CatalogAuditListener {
    private final JdbcTemplate db;
    private final CatalogMessageCodec codec;

    @JmsListener(destination = CatalogOutbox.AUDIT_QUEUE)
    public void receiveChange(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            var change = codec.decodeChange(textMessage.getText());
            if (change.isPresent()) {
                insertAuditEntry(change.orElseThrow());
                return;
            }
        }
        log.warn("Ignored invalid catalog audit message");
    }

    private void insertAuditEntry(CatalogChange change) {
        db.update("""
                INSERT INTO catalog_audit(event_id, occurred_at, operation, entity_type, entity_id, change_details)
                VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb))
                ON CONFLICT (event_id) DO NOTHING
                """, change.eventId(), OffsetDateTime.ofInstant(change.occurredAt(), ZoneOffset.UTC),
                change.operation().name(), change.entity().name(), change.entityId(), codec.encodeDetails(change));
    }
}
