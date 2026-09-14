package com.eop.operations.infrastructure.persistence;

import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incidents_service_env", columnList = "service_id, env_id"),
        @Index(name = "idx_incidents_deploy_id", columnList = "deploy_id")
})
public class IncidentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "env_id", nullable = false)
    private UUID envId;

    @Column(name = "deploy_id")
    private UUID deployId;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private IncidentStatus status;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public IncidentJpaEntity() {}

    public IncidentJpaEntity(UUID id, UUID serviceId, UUID envId, UUID deployId, IncidentSeverity severity,
                             IncidentStatus status, String title, String summary, Instant createdAt, Instant resolvedAt) {
        this.id = id;
        this.serviceId = serviceId;
        this.envId = envId;
        this.deployId = deployId;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.summary = summary;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public UUID getEnvId() { return envId; }
    public void setEnvId(UUID envId) { this.envId = envId; }
    public UUID getDeployId() { return deployId; }
    public void setDeployId(UUID deployId) { this.deployId = deployId; }
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
