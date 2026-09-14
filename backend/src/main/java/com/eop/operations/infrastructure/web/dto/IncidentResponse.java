package com.eop.operations.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    UUID serviceId,
    UUID envId,
    UUID deployId,
    String severity,
    String status,
    String title,
    String summary,
    Instant createdAt
) {
    public IncidentResponse(
            UUID id,
            UUID serviceId,
            UUID envId,
            String severity,
            String status,
            String summary,
            Instant createdAt
    ) {
        this(id, serviceId, envId, null, severity, status, null, summary, createdAt);
    }
}
