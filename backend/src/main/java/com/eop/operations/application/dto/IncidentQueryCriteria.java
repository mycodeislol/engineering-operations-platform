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
        if (severities != null && !severities.isEmpty()) {
            severities = List.copyOf(severities);
        }
    }
}
