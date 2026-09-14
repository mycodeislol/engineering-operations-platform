package com.eop.operations.infrastructure.persistence;

import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<IncidentJpaEntity, UUID> {
    Optional<IncidentJpaEntity> findByDeployId(UUID deployId);

    @Query("""
        SELECT i FROM IncidentJpaEntity i
        WHERE i.serviceId = :serviceId
          AND i.envId = :envId
          AND i.severity IN :severities
          AND (:status IS NULL OR i.status = :status)
        ORDER BY i.createdAt DESC
    """)
    List<IncidentJpaEntity> findByServiceAndEnvAndSeverities(
            @Param("serviceId") UUID serviceId,
            @Param("envId") UUID envId,
            @Param("severities") Collection<IncidentSeverity> severities,
            @Param("status") IncidentStatus status
    );
}
