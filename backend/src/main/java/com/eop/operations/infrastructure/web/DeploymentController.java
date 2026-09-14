package com.eop.operations.infrastructure.web;

import com.eop.operations.application.GetDeploymentsService;
import com.eop.operations.application.UpdateDeploymentStatusService;
import com.eop.operations.infrastructure.web.dto.DeploymentResponse;
import com.eop.operations.infrastructure.web.dto.UpdateDeploymentStatusRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/deployments")
@CrossOrigin(origins = "*")
public class DeploymentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);

    private final UpdateDeploymentStatusService updateStatusService;
    private final GetDeploymentsService getDeploymentsService;

    public DeploymentController(
            UpdateDeploymentStatusService updateStatusService,
            @Autowired(required = false) GetDeploymentsService getDeploymentsService
    ) {
        this.updateStatusService = Objects.requireNonNull(updateStatusService, "updateStatusService cannot be null");
        this.getDeploymentsService = getDeploymentsService;
    }

    @GetMapping
    public ResponseEntity<List<DeploymentResponse>> getDeployments() {
        if (getDeploymentsService == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(getDeploymentsService.execute());
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
