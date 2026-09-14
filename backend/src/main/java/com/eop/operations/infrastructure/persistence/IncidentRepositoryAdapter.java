package com.eop.operations.infrastructure.persistence;

import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.model.Incident;
import com.eop.operations.domain.model.IncidentId;
import com.eop.operations.domain.repository.IncidentRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class IncidentRepositoryAdapter implements IncidentRepository {

    private final SpringDataIncidentRepository springDataRepo;

    public IncidentRepositoryAdapter(SpringDataIncidentRepository springDataRepo) {
        this.springDataRepo = Objects.requireNonNull(springDataRepo, "springDataRepo cannot be null");
    }

    @Override
    public Optional<Incident> findById(IncidentId id) {
        Objects.requireNonNull(id, "IncidentId cannot be null");
        return springDataRepo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Incident> findByDeploymentId(DeploymentId deploymentId) {
        Objects.requireNonNull(deploymentId, "DeploymentId cannot be null");
        return springDataRepo.findByDeployId(deploymentId.value()).map(this::toDomain);
    }

    @Override
    public Incident save(Incident incident) {
        Objects.requireNonNull(incident, "Incident cannot be null");
        IncidentJpaEntity entity = toEntity(incident);
        IncidentJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    private Incident toDomain(IncidentJpaEntity entity) {
        return new Incident(
                IncidentId.of(entity.getId()),
                entity.getServiceId(),
                entity.getEnvId(),
                entity.getDeployId() != null ? DeploymentId.of(entity.getDeployId()) : null,
                entity.getSeverity(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }

    private IncidentJpaEntity toEntity(Incident domain) {
        return new IncidentJpaEntity(
                domain.getId().value(),
                domain.getServiceId(),
                domain.getEnvId(),
                domain.getDeploymentId() != null ? domain.getDeploymentId().value() : null,
                domain.getSeverity(),
                domain.getStatus(),
                domain.getTitle(),
                domain.getSummary(),
                domain.getCreatedAt(),
                domain.getResolvedAt()
        );
    }
}
