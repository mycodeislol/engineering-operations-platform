package com.eop.operations.infrastructure.web;

import com.eop.operations.application.dto.DeploymentStatusResponseDto;
import com.eop.operations.application.dto.UpdateDeploymentStatusCommand;
import com.eop.operations.application.port.in.UpdateDeploymentStatusUseCase;
import com.eop.operations.infrastructure.web.request.UpdateDeploymentStatusHttpRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deployments")
public class DeploymentRestController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentRestController.class);

    private final UpdateDeploymentStatusUseCase updateDeploymentStatusUseCase;

    public DeploymentRestController(UpdateDeploymentStatusUseCase updateDeploymentStatusUseCase) {
        this.updateDeploymentStatusUseCase = Objects.requireNonNull(updateDeploymentStatusUseCase, "updateDeploymentStatusUseCase cannot be null");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeploymentStatusResponseDto> updateDeploymentStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateDeploymentStatusHttpRequest request
    ) {
        log.info("Received HTTP PATCH /api/v1/deployments/{}/status with targetStatus=[{}]", id, request.status());

        UpdateDeploymentStatusCommand command = new UpdateDeploymentStatusCommand(
                id,
                request.status(),
                request.failureReason()
        );

        DeploymentStatusResponseDto response = updateDeploymentStatusUseCase.updateStatus(command);
        return ResponseEntity.ok(response);
    }
}
