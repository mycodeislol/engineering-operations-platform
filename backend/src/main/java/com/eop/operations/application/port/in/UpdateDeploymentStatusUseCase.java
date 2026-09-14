package com.eop.operations.application.port.in;

import com.eop.operations.application.dto.DeploymentStatusResponseDto;
import com.eop.operations.application.dto.UpdateDeploymentStatusCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UpdateDeploymentStatusUseCase {
    DeploymentStatusResponseDto updateStatus(@NotNull @Valid UpdateDeploymentStatusCommand command);
}
