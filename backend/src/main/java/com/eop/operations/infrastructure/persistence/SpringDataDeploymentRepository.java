package com.eop.operations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataDeploymentRepository extends JpaRepository<DeploymentJpaEntity, UUID> {}
