package com.eop.operations.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record DeploymentResponse(
        UUID id,
        UUID buildId,
        UUID serviceId,
        UUID envId,
        String status,
        String failureReason,
        Instant startedAt,
        Instant completedAt
) {}
