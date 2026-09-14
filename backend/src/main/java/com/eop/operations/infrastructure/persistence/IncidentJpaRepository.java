package com.eop.operations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentJpaEntity, UUID> {

    @Query("SELECT i FROM IncidentJpaEntity i WHERE (:serviceId IS NULL OR i.serviceId = :serviceId) " +
           "AND (:envId IS NULL OR i.envId = :envId) " +
           "AND (:severity IS NULL OR CAST(i.severity AS string) = :severity OR REPLACE(CAST(i.severity AS string), '_', '') = :severity)")
    List<IncidentJpaEntity> findByCompositeFilters(
        @Param("serviceId") UUID serviceId,
        @Param("envId") UUID envId,
        @Param("severity") String severity
    );
}
