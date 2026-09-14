package com.eop.operations.domain.repository;

import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.model.IncidentId;
import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {
    Optional<Incident> findById(IncidentId id);
    Optional<Incident> findByDeploymentId(DeploymentId deploymentId);
    List<Incident> findByFilters(UUID serviceId, UUID envId, Collection<IncidentSeverity> severities, IncidentStatus status);
    Incident save(Incident incident);
}
