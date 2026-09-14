package com.eop.operations.application;

import com.eop.operations.infrastructure.persistence.SpringDataDeploymentRepository;
import com.eop.operations.infrastructure.web.dto.DeploymentResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetDeploymentsService {

    private final SpringDataDeploymentRepository deploymentRepository;

    public GetDeploymentsService(SpringDataDeploymentRepository deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    public List<DeploymentResponse> execute() {
        return deploymentRepository.findAll(Sort.by(Sort.Direction.DESC, "startedAt")).stream()
                .map(d -> new DeploymentResponse(
                        d.getId(),
                        d.getBuildId(),
                        d.getServiceId(),
                        d.getEnvId(),
                        d.getStatus() != null ? d.getStatus().name() : null,
                        d.getFailureReason(),
                        d.getStartedAt(),
                        d.getCompletedAt()
                ))
                .toList();
    }
}
