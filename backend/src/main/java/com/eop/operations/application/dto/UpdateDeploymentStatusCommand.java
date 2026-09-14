package com.eop.operations.application.dto;

import com.eop.operations.domain.model.DeploymentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateDeploymentStatusCommand(
        @NotNull(message = "Deployment ID is required")
        UUID deploymentId,

        @NotNull(message = "Target deployment status is required")
        DeploymentStatus targetStatus,

        String failureReason
) {}
