package com.eop.operations.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Incident {
    private final IncidentId id;
    private final UUID serviceId;
    private final UUID envId;
    private final DeploymentId deploymentId;
    private final IncidentSeverity severity;
    private IncidentStatus status;
    private final String title;
    private final String summary;
    private final Instant createdAt;
    private Instant resolvedAt;

    public Incident(IncidentId id, UUID serviceId, UUID envId, DeploymentId deploymentId,
                    IncidentSeverity severity, IncidentStatus status, String title, String summary, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Incident id cannot be null");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId cannot be null");
        this.envId = Objects.requireNonNull(envId, "envId cannot be null");
        this.deploymentId = deploymentId;
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.summary = summary;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public Incident(IncidentId id, UUID serviceId, UUID envId, DeploymentId deploymentId,
                    IncidentSeverity severity, IncidentStatus status, String title, String summary,
                    Instant createdAt, Instant resolvedAt) {
        this(id, serviceId, envId, deploymentId, severity, status, title, summary, createdAt);
        this.resolvedAt = resolvedAt;
    }

    public static Incident createAutomatedFromFailedDeployment(Deployment deployment, String diagnosticMessage) {
        Objects.requireNonNull(deployment, "Deployment must not be null to trigger automated incident");
        String incidentTitle = String.format("Automated Alert: Deployment Failure [%s]", deployment.getId().value());
        String incidentSummary = String.format("Deployment [%s] targeting environment [%s] transitioned to FAILED. Diagnostic root cause: %s",
                deployment.getId().value(), deployment.getEnvId(),
                (diagnosticMessage == null || diagnosticMessage.isBlank()) ? "Unspecified pipeline failure" : diagnosticMessage);

        return new Incident(
                IncidentId.generate(),
                deployment.getServiceId(),
                deployment.getEnvId(),
                deployment.getId(),
                IncidentSeverity.SEV_2,
                IncidentStatus.OPEN,
                incidentTitle,
                incidentSummary,
                Instant.now()
        );
    }

    public void transitionTo(IncidentStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "Target incident status cannot be null");
        if (this.status == IncidentStatus.RESOLVED && targetStatus != IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Cannot transition resolved incident [" + this.id.value() + "] back to " + targetStatus);
        }
        if (targetStatus == IncidentStatus.RESOLVED) {
            this.resolvedAt = Instant.now();
        }
        this.status = targetStatus;
    }

    public IncidentId getId() {
        return id;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getEnvId() {
        return envId;
    }

    public DeploymentId getDeploymentId() {
        return deploymentId;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
