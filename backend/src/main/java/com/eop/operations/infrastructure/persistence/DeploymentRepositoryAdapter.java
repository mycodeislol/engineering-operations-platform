package com.eop.operations.infrastructure.persistence;

import com.eop.operations.domain.model.Deployment;
import com.eop.operations.domain.model.DeploymentId;
import com.eop.operations.domain.repository.DeploymentRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class DeploymentRepositoryAdapter implements DeploymentRepository {

    private final SpringDataDeploymentRepository springDataRepo;

    public DeploymentRepositoryAdapter(SpringDataDeploymentRepository springDataRepo) {
        this.springDataRepo = Objects.requireNonNull(springDataRepo, "springDataRepo cannot be null");
    }

    @Override
    public Optional<Deployment> findById(DeploymentId id) {
        Objects.requireNonNull(id, "DeploymentId cannot be null");
        return springDataRepo.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Deployment save(Deployment deployment) {
        Objects.requireNonNull(deployment, "Deployment cannot be null");
        DeploymentJpaEntity entity = toEntity(deployment);
        DeploymentJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    private Deployment toDomain(DeploymentJpaEntity entity) {
        return new Deployment(
                DeploymentId.of(entity.getId()),
                entity.getBuildId(),
                entity.getServiceId(),
                entity.getEnvId(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }

    private DeploymentJpaEntity toEntity(Deployment domain) {
        return new DeploymentJpaEntity(
                domain.getId().value(),
                domain.getBuildId(),
                domain.getServiceId(),
                domain.getEnvId(),
                domain.getStatus(),
                domain.getFailureReason(),
                domain.getStartedAt(),
                domain.getCompletedAt()
        );
    }
}
