package com.eop.operations.application.dto;

import com.eop.operations.domain.model.DeploymentStatus;

import java.time.Instant;
import java.util.UUID;

public record DeploymentStatusResponseDto(
        UUID deploymentId,
        UUID serviceId,
        UUID envId,
        DeploymentStatus status,
        String failureReason,
        Instant startedAt,
        Instant completedAt,
        UUID triggeredIncidentId
) {}
