package com.eop.operations.domain.model;

import com.eop.operations.domain.exception.InvalidDeploymentStateTransitionException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Deployment {
    private final DeploymentId id;
    private final UUID buildId;
    private final UUID serviceId;
    private final UUID envId;
    private DeploymentStatus status;
    private final Instant startedAt;
    private Instant completedAt;
    private String failureReason;

    public Deployment(DeploymentId id, UUID buildId, UUID serviceId, UUID envId, DeploymentStatus status, Instant startedAt) {
        this.id = Objects.requireNonNull(id, "Deployment id cannot be null");
        this.buildId = Objects.requireNonNull(buildId, "buildId cannot be null");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId cannot be null");
        this.envId = Objects.requireNonNull(envId, "envId cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt cannot be null");
    }

    public Deployment(DeploymentId id, UUID buildId, UUID serviceId, UUID envId, DeploymentStatus status,
                      String failureReason, Instant startedAt, Instant completedAt) {
        this(id, buildId, serviceId, envId, status, startedAt);
        this.failureReason = failureReason;
        this.completedAt = completedAt;
    }

    public void transitionTo(DeploymentStatus targetStatus, String reason) {
        Objects.requireNonNull(targetStatus, "Target deployment status must not be null");

        if (this.status.isTerminal()) {
            throw new InvalidDeploymentStateTransitionException(this.id, this.status, targetStatus);
        }

        switch (targetStatus) {
            case RUNNING -> {
                if (this.status != DeploymentStatus.QUEUED) {
                    throw new InvalidDeploymentStateTransitionException(this.id, this.status, targetStatus);
                }
            }
            case SUCCESS -> {
                if (this.status != DeploymentStatus.RUNNING) {
                    throw new InvalidDeploymentStateTransitionException(this.id, this.status, targetStatus);
                }
                this.completedAt = Instant.now();
            }
            case FAILED -> {
                if (this.status != DeploymentStatus.RUNNING && this.status != DeploymentStatus.QUEUED) {
                    throw new InvalidDeploymentStateTransitionException(this.id, this.status, targetStatus);
                }
                this.failureReason = (reason == null || reason.isBlank()) ? "Unspecified pipeline error" : reason;
                this.completedAt = Instant.now();
            }
            case QUEUED -> throw new InvalidDeploymentStateTransitionException(this.id, this.status, targetStatus);
        }

        this.status = targetStatus;
    }

    public DeploymentId getId() {
        return id;
    }

    public UUID getBuildId() {
        return buildId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getEnvId() {
        return envId;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
