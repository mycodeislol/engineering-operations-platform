package com.eop.operations.infrastructure.web.request;

import com.eop.operations.domain.model.DeploymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeploymentStatusHttpRequest(
        @NotNull(message = "Target status is required")
        DeploymentStatus status,

        String failureReason
) {}
