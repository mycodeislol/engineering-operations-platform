package com.eop.operations.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDeploymentStatusRequest(
        @NotNull(message = "Status cannot be null")
        @NotBlank(message = "Status cannot be blank")
        String status,

        String triggeredBy
) {}
