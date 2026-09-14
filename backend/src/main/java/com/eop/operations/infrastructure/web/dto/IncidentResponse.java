package com.eop.operations.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    UUID serviceId,
    UUID envId,
    String severity,
    String status,
    String summary,
    Instant createdAt
) {}
