package com.eop.operations.application.dto;

import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record IncidentResponseDto(
        UUID id,
        UUID serviceId,
        UUID envId,
        UUID deployId,
        IncidentSeverity severity,
        IncidentStatus status,
        String title,
        String summary,
        Instant createdAt,
        Instant resolvedAt
) {
    public static IncidentResponseDto fromDomain(Incident incident) {
        Objects.requireNonNull(incident, "Incident cannot be null");
        return new IncidentResponseDto(
                incident.getId().value(),
                incident.getServiceId(),
                incident.getEnvId(),
                incident.getDeploymentId() != null ? incident.getDeploymentId().value() : null,
                incident.getSeverity(),
                incident.getStatus(),
                incident.getTitle(),
                incident.getSummary(),
                incident.getCreatedAt(),
                incident.getResolvedAt()
        );
    }
}
