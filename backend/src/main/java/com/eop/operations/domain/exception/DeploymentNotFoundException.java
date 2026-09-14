package com.eop.operations.domain.exception;

import com.eop.operations.domain.model.DeploymentId;

public class DeploymentNotFoundException extends RuntimeException {
    private final DeploymentId deploymentId;

    public DeploymentNotFoundException(DeploymentId deploymentId) {
        super("Deployment with identifier [" + (deploymentId != null ? deploymentId.value() : "null") + "] was not found.");
        this.deploymentId = deploymentId;
    }

    public DeploymentId getDeploymentId() {
        return deploymentId;
    }
}
