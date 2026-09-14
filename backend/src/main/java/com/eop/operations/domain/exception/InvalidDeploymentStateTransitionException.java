package com.eop.operations.domain.exception;

import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.model.DeploymentStatus;

public class InvalidDeploymentStateTransitionException extends RuntimeException {
    private final DeploymentId deploymentId;
    private final DeploymentStatus currentStatus;
    private final DeploymentStatus targetStatus;

    public InvalidDeploymentStateTransitionException(DeploymentId deploymentId, DeploymentStatus currentStatus, DeploymentStatus targetStatus) {
        super(String.format("Illegal deployment state transition for [%s]: Cannot transition from [%s] to [%s].",
                deploymentId != null ? deploymentId.value() : "unknown", currentStatus, targetStatus));
        this.deploymentId = deploymentId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public DeploymentId getDeploymentId() {
        return deploymentId;
    }

    public DeploymentStatus getCurrentStatus() {
        return currentStatus;
    }

    public DeploymentStatus getTargetStatus() {
        return targetStatus;
    }
}
