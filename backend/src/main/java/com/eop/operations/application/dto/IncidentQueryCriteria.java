package com.eop.operations.application.dto;

import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record IncidentQueryCriteria(
        UUID serviceId,
        UUID envId,
        List<IncidentSeverity> severities,
        IncidentStatus status
) {
    public IncidentQueryCriteria {
        Objects.requireNonNull(serviceId, "serviceId cannot be null");
        Objects.requireNonNull(envId, "envId cannot be null");
        if (severities == null || severities.isEmpty()) {
            severities = List.of(IncidentSeverity.SEV_2, IncidentSeverity.SEV_3);
        } else {
            severities = List.copyOf(severities);
        }
    }
}
