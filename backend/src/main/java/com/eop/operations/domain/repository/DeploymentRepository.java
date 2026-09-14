package com.eop.operations.domain.repository;

import com.eop.operations.domain.model.Deployment;
import com.eop.operations.domain.model.DeploymentId;

import java.util.Optional;

public interface DeploymentRepository {
    Optional<Deployment> findById(DeploymentId id);
    Deployment save(Deployment deployment);
}
