package com.eop.operations.domain.repository;

import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.model.IncidentId;

import java.util.Optional;

public interface IncidentRepository {
    Optional<Incident> findById(IncidentId id);
    Optional<Incident> findByDeploymentId(DeploymentId deploymentId);
    Incident save(Incident incident);
}
