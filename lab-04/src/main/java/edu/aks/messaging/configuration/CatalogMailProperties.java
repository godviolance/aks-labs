package edu.aks.messaging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties("catalog.mail")
public record CatalogMailProperties(String sender, List<String> recipients) {
    public CatalogMailProperties {
        recipients = recipients.stream().map(String::strip).filter(address -> !address.isEmpty()).toList();
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one catalog notification recipient is required");
        }
    }
}
