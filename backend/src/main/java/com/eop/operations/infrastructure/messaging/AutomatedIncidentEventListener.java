package com.eop.operations.infrastructure.messaging;

import com.eop.operations.domain.event.DeploymentFailedEvent;
import com.eop.operations.domain.model.Deployment;
import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.repository.DeploymentRepository;
import com.eop.operations.domain.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Component
public class AutomatedIncidentEventListener {

    private static final Logger log = LoggerFactory.getLogger(AutomatedIncidentEventListener.class);

    private final IncidentRepository incidentRepository;
    private final DeploymentRepository deploymentRepository;

    public AutomatedIncidentEventListener(IncidentRepository incidentRepository, DeploymentRepository deploymentRepository) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "incidentRepository must not be null");
        this.deploymentRepository = Objects.requireNonNull(deploymentRepository, "deploymentRepository must not be null");
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleDeploymentFailure(DeploymentFailedEvent event) {
        Objects.requireNonNull(event, "DeploymentFailedEvent must not be null");

        log.info("Processing DeploymentFailedEvent: deploymentId=[{}], serviceId=[{}], envId=[{}]",
                event.deploymentId().value(), event.serviceId(), event.envId());

        Optional<Incident> existingIncident = incidentRepository.findByDeploymentId(event.deploymentId());
        if (existingIncident.isPresent()) {
            log.warn("Incident [{}] already exists for failed deployment [{}]. Skipping duplicate creation.",
                    existingIncident.get().getId().value(), event.deploymentId().value());
            return;
        }

        Deployment deployment = deploymentRepository.findById(event.deploymentId())
                .orElseThrow(() -> new IllegalStateException("Deployment entity missing during failure event processing: "
                        + event.deploymentId().value()));

        Incident autoIncident = Incident.createAutomatedFromFailedDeployment(deployment, event.failureReason());
        Incident savedIncident = incidentRepository.save(autoIncident);

        log.info("SUCCESS: Automated SEV-2 Incident [{}] created for failed deployment [{}] on service [{}]",
                savedIncident.getId().value(), deployment.getId().value(), deployment.getServiceId());
    }
}
