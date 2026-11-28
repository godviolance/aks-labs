package edu.aks.catalog.dto;

public record CatalogEventInput(
        String id, String title, String description, String courseId, String topic, String minutes
) {}
