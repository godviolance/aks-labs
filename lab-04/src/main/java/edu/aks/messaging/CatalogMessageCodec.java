package edu.aks.messaging;

import edu.aks.catalog.model.CatalogChange;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import java.util.Optional;

@Component
public class CatalogMessageCodec {
    private final JsonMapper mapper = JsonMapper.builder().build();

    public String encodeChange(CatalogChange change) {
        return mapper.writeValueAsString(change);
    }

    public Optional<CatalogChange> decodeChange(String payload) {
        if (payload == null) {
            return Optional.empty();
        }
        try {
            CatalogChange change = mapper.readValue(payload, CatalogChange.class);
            return isValidChange(change) ? Optional.of(change) : Optional.empty();
        } catch (tools.jackson.core.JacksonException ignored) {
            return Optional.empty();
        }
    }

    public String encodeDetails(CatalogChange change) {
        return mapper.writeValueAsString(change.details());
    }

    private boolean isValidChange(CatalogChange change) {
        if (change == null || change.eventId() == null || change.occurredAt() == null || change.operation() == null
                || change.entity() == null || change.entityId() <= 0 || change.details() == null) {
            return false;
        }
        return switch (change.entity()) {
            case COURSE -> change.details().get("title") != null && change.details().get("description") != null;
            case LESSON -> change.details().get("topic") != null && change.details().get("minutes") != null
                    && change.details().get("courseId") != null;
        };
    }
}
