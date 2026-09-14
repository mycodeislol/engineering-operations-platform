package com.eop.operations.infrastructure.persistence;

import com.eop.operations.domain.model.DeploymentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deployments", indexes = {
        @Index(name = "idx_deployments_service_env", columnList = "service_id, env_id"),
        @Index(name = "idx_deployments_status", columnList = "status")
})
public class DeploymentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "build_id", nullable = false)
    private UUID buildId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "env_id", nullable = false)
    private UUID envId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DeploymentStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public DeploymentJpaEntity() {}

    public DeploymentJpaEntity(UUID id, UUID buildId, UUID serviceId, UUID envId, DeploymentStatus status,
                               String failureReason, Instant startedAt, Instant completedAt) {
        this.id = id;
        this.buildId = buildId;
        this.serviceId = serviceId;
        this.envId = envId;
        this.status = status;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBuildId() { return buildId; }
    public void setBuildId(UUID buildId) { this.buildId = buildId; }
    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public UUID getEnvId() { return envId; }
    public void setEnvId(UUID envId) { this.envId = envId; }
    public DeploymentStatus getStatus() { return status; }
    public void setStatus(DeploymentStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
