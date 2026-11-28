package edu.aks.catalog.web;

public record CatalogEventInput(
        String id, String title, String description, String courseId, String topic, String minutes
) {}
