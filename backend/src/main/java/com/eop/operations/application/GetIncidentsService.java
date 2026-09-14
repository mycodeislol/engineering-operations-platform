package com.eop.operations.application;

import com.eop.operations.infrastructure.persistence.IncidentJpaRepository;
import com.eop.operations.infrastructure.web.dto.IncidentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetIncidentsService {

    private final IncidentJpaRepository incidentRepository;

    public GetIncidentsService(IncidentJpaRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<IncidentResponse> execute(UUID serviceId, UUID envId, String severity) {
        return incidentRepository.findByCompositeFilters(serviceId, envId, severity).stream()
                .map(entity -> new IncidentResponse(
                        entity.getId(),
                        entity.getServiceId(),
                        entity.getEnvId(),
                        entity.getDeployId(),
                        entity.getSeverity() != null ? entity.getSeverity().name() : null,
                        entity.getStatus() != null ? entity.getStatus().name() : null,
                        entity.getTitle(),
                        entity.getSummary(),
                        entity.getCreatedAt()
                ))
                .toList();
    }
}
