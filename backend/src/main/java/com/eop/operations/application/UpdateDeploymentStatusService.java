package com.eop.operations.application;

import com.eop.operations.application.dto.UpdateDeploymentStatusCommand;
import com.eop.operations.application.port.in.UpdateDeploymentStatusUseCase;
import com.eop.operations.domain.model.DeploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service("facadeUpdateDeploymentStatusService")
public class UpdateDeploymentStatusService {

    private static final Logger log = LoggerFactory.getLogger(UpdateDeploymentStatusService.class);

    private final UpdateDeploymentStatusUseCase updateDeploymentStatusUseCase;

    public UpdateDeploymentStatusService(UpdateDeploymentStatusUseCase updateDeploymentStatusUseCase) {
        this.updateDeploymentStatusUseCase = Objects.requireNonNull(updateDeploymentStatusUseCase, "updateDeploymentStatusUseCase cannot be null");
    }

    public void execute(UUID id, String status, String triggeredBy) {
        Objects.requireNonNull(id, "Deployment id cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");

        log.info("Executing UpdateDeploymentStatusService for id=[{}], status=[{}], triggeredBy=[{}]", id, status, triggeredBy);

        DeploymentStatus targetStatus;
        try {
            targetStatus = DeploymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid deployment status: " + status, e);
        }

        UpdateDeploymentStatusCommand command = new UpdateDeploymentStatusCommand(id, targetStatus, triggeredBy);
        updateDeploymentStatusUseCase.updateStatus(command);
    }
}
