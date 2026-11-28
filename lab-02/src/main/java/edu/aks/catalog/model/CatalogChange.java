package edu.aks.catalog.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CatalogChange(UUID eventId, Instant occurredAt, ChangeOperation operation,
                            EntityType entity, long entityId, Map<String, String> details) {}
