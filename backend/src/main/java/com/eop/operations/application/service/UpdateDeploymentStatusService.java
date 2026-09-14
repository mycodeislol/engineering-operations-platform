package com.eop.operations.application.service;

import com.eop.operations.application.dto.DeploymentStatusResponseDto;
import com.eop.operations.application.dto.UpdateDeploymentStatusCommand;
import com.eop.operations.application.port.in.UpdateDeploymentStatusUseCase;
import com.eop.operations.application.port.out.DomainEventPublisherPort;
import com.eop.operations.domain.event.DeploymentFailedEvent;
import com.eop.operations.domain.exception.DeploymentNotFoundException;
import com.eop.operations.domain.model.Deployment;
import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.model.DeploymentStatus;
import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.repository.DeploymentRepository;
import com.eop.operations.domain.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
public class UpdateDeploymentStatusService implements UpdateDeploymentStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateDeploymentStatusService.class);

    private final DeploymentRepository deploymentRepository;
    private final IncidentRepository incidentRepository;
    private final DomainEventPublisherPort eventPublisher;

    public UpdateDeploymentStatusService(
            DeploymentRepository deploymentRepository,
            IncidentRepository incidentRepository,
            DomainEventPublisherPort eventPublisher
    ) {
        this.deploymentRepository = Objects.requireNonNull(deploymentRepository, "deploymentRepository cannot be null");
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "incidentRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public DeploymentStatusResponseDto updateStatus(UpdateDeploymentStatusCommand command) {
        long startTime = System.currentTimeMillis();
        DeploymentId deploymentId = DeploymentId.of(command.deploymentId());
        MDC.put("deploymentId", deploymentId.value().toString());

        log.info("Entering updateDeploymentStatus: deploymentId=[{}], targetStatus=[{}], reason=[{}]",
                deploymentId.value(), command.targetStatus(), command.failureReason());

        try {
            Deployment deployment = deploymentRepository.findById(deploymentId)
                    .orElseThrow(() -> new DeploymentNotFoundException(deploymentId));

            DeploymentStatus previousStatus = deployment.getStatus();
            deployment.transitionTo(command.targetStatus(), command.failureReason());

            Deployment savedDeployment = deploymentRepository.save(deployment);
            log.debug("Deployment state updated in repository: previous=[{}], current=[{}]",
                    previousStatus, savedDeployment.getStatus());

            UUID triggeredIncidentId = null;

            if (savedDeployment.getStatus() == DeploymentStatus.FAILED) {
                log.warn("Deployment failure detected. Emitting synchronous DeploymentFailedEvent for deployment [{}]",
                        deploymentId.value());

                DeploymentFailedEvent event = new DeploymentFailedEvent(
                        savedDeployment.getId(),
                        savedDeployment.getServiceId(),
                        savedDeployment.getEnvId(),
                        savedDeployment.getFailureReason(),
                        Instant.now()
                );
                eventPublisher.publish(event);

                Optional<Incident> associatedIncident = incidentRepository.findByDeploymentId(deploymentId);
                if (associatedIncident.isPresent()) {
                    triggeredIncidentId = associatedIncident.get().getId().value();
                    log.info("Discovered linked Sev-2 Incident [{}] created for failed deployment [{}]",
                            triggeredIncidentId, deploymentId.value());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Exiting updateDeploymentStatus: deploymentId=[{}], finalStatus=[{}], durationMs=[{}]",
                    deploymentId.value(), savedDeployment.getStatus(), duration);

            return new DeploymentStatusResponseDto(
                    savedDeployment.getId().value(),
                    savedDeployment.getServiceId(),
                    savedDeployment.getEnvId(),
                    savedDeployment.getStatus(),
                    savedDeployment.getFailureReason(),
                    savedDeployment.getStartedAt(),
                    savedDeployment.getCompletedAt(),
                    triggeredIncidentId
            );
        } finally {
            MDC.remove("deploymentId");
        }
    }
}
