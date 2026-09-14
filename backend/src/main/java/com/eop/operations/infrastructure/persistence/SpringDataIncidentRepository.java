package com.eop.operations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<IncidentJpaEntity, UUID> {
    Optional<IncidentJpaEntity> findByDeployId(UUID deployId);
}
