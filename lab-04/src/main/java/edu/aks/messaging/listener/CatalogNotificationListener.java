package edu.aks.messaging.listener;

import edu.aks.catalog.model.CatalogChange;
import edu.aks.catalog.model.ChangeOperation;
import edu.aks.catalog.model.EntityType;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import edu.aks.messaging.service.CatalogMessageCodec;
import edu.aks.messaging.service.CatalogOutbox;
import lombok.extern.slf4j.Slf4j;
import edu.aks.messaging.configuration.CatalogMailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CatalogNotificationListener {
    private final JdbcTemplate db;
    private final MailSender mail;
    private final CatalogMessageCodec codec;
    private final CatalogMailProperties mailProperties;

    @JmsListener(destination = CatalogOutbox.NOTIFY_QUEUE)
    @Transactional
    public void receiveChange(Message message) throws JMSException {
        if (!(message instanceof TextMessage textMessage)) {
            log.warn("Ignored non-text catalog notification");
            return;
        }
        var decoded = codec.decodeChange(textMessage.getText());
        if (decoded.isEmpty()) {
            log.warn("Ignored invalid catalog notification");
            return;
        }
        CatalogChange change = decoded.orElseThrow();
        if (change.entity() == EntityType.COURSE && change.operation() == ChangeOperation.DELETE) {
            sendCourseDeletion(change);
        }
    }

    private void sendCourseDeletion(CatalogChange change) {
        int claimed = db.update("""
                INSERT INTO catalog_notification(event_id) VALUES (?)
                ON CONFLICT (event_id) DO NOTHING
                """, change.eventId());
        if (claimed != 1) {
            return;
        }
        String title = change.details().get("title");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.sender());
        message.setTo(mailProperties.recipients().toArray(String[]::new));
        message.setSubject("Удалён курс: " + title.replaceAll("[\\r\\n]+", " "));
        message.setText("Название: " + title + "\nID: " + change.entityId() + "\nСобытие: " + change.eventId());
        mail.send(message);
    }
}
