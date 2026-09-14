package com.eop.operations.infrastructure.web.request;

import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record IncidentQueryWebRequest(
        @NotNull(message = "serviceId is required")
        UUID serviceId,

        @NotNull(message = "envId is required")
        UUID envId,

        List<IncidentSeverity> severities,

        IncidentStatus status
) {}
