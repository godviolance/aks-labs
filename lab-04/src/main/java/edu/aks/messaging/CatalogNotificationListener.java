package edu.aks.messaging;

import edu.aks.catalog.model.CatalogChange;
import edu.aks.catalog.model.ChangeOperation;
import edu.aks.catalog.model.EntityType;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Component
public class CatalogNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(CatalogNotificationListener.class);
    private final JdbcTemplate db;
    private final MailSender mail;
    private final CatalogMessageCodec codec;
    private final String sender;
    private final String[] recipients;

    public CatalogNotificationListener(JdbcTemplate db, MailSender mail, CatalogMessageCodec codec,
                                       @Value("${catalog.mail.sender}") String sender,
                                       @Value("${catalog.mail.recipients}") String recipients) {
        this.db = db;
        this.mail = mail;
        this.codec = codec;
        this.sender = sender;
        this.recipients = Arrays.stream(recipients.split(",")).map(String::strip)
                .filter(address -> !address.isEmpty()).toArray(String[]::new);
        if (this.recipients.length == 0) {
            throw new IllegalArgumentException("At least one catalog notification recipient is required");
        }
    }

    @JmsListener(destination = CatalogOutbox.NOTIFY_QUEUE)
    @Transactional
    public void receiveChange(Message message) throws JMSException {
        if (!(message instanceof TextMessage textMessage)) {
            LOG.warn("Ignored non-text catalog notification");
            return;
        }
        var decoded = codec.decodeChange(textMessage.getText());
        if (decoded.isEmpty()) {
            LOG.warn("Ignored invalid catalog notification");
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
        message.setFrom(sender);
        message.setTo(recipients);
        message.setSubject("Удалён курс: " + title.replaceAll("[\\r\\n]+", " "));
        message.setText("Название: " + title + "\nID: " + change.entityId() + "\nСобытие: " + change.eventId());
        mail.send(message);
    }
}
