package com.eop.operations.domain.event;

import com.eop.operations.domain.model.DeploymentId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DeploymentFailedEvent(
        DeploymentId deploymentId,
        UUID serviceId,
        UUID envId,
        String failureReason,
        Instant occurredAt
) {
    public DeploymentFailedEvent {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(envId, "envId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
