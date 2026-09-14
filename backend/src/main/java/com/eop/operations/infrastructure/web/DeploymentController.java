package com.eop.operations.infrastructure.web;

import com.eop.operations.application.UpdateDeploymentStatusService;
import com.eop.operations.infrastructure.web.dto.UpdateDeploymentStatusRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/deployments")
public class DeploymentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);

    private final UpdateDeploymentStatusService updateStatusService;

    public DeploymentController(UpdateDeploymentStatusService updateStatusService) {
        this.updateStatusService = Objects.requireNonNull(updateStatusService, "updateStatusService cannot be null");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateDeploymentStatusRequest request
    ) {
        log.info("Received HTTP PATCH /api/v1/operations/deployments/{}/status with status=[{}], triggeredBy=[{}]",
                id, request.status(), request.triggeredBy());

        updateStatusService.execute(id, request.status(), request.triggeredBy());
        return ResponseEntity.noContent().build();
    }
}
