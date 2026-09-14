package com.eop.operations.application.service;

import com.eop.operations.application.dto.IncidentQueryCriteria;
import com.eop.operations.application.dto.IncidentResponseDto;
import com.eop.operations.application.port.in.QueryIncidentsUseCase;
import com.eop.operations.domain.model.IncidentId;
import com.eop.operations.domain.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@Validated
@Transactional(
        propagation = Propagation.SUPPORTS,
        isolation = Isolation.READ_COMMITTED,
        readOnly = true
)
public class QueryIncidentsService implements QueryIncidentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(QueryIncidentsService.class);

    private final IncidentRepository incidentRepository;

    public QueryIncidentsService(IncidentRepository incidentRepository) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "incidentRepository cannot be null");
    }

    @Override
    public List<IncidentResponseDto> queryIncidents(IncidentQueryCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria cannot be null");
        MDC.put("serviceId", criteria.serviceId().toString());
        MDC.put("envId", criteria.envId().toString());
        try {
            log.info("Querying incidents: serviceId=[{}], envId=[{}], severities={}, status={}",
                    criteria.serviceId(), criteria.envId(), criteria.severities(), criteria.status());

            return incidentRepository.findByFilters(
                            criteria.serviceId(),
                            criteria.envId(),
                            criteria.severities(),
                            criteria.status()
                    )
                    .stream()
                    .map(IncidentResponseDto::fromDomain)
                    .toList();
        } finally {
            MDC.remove("serviceId");
            MDC.remove("envId");
        }
    }

    @Override
    public IncidentResponseDto getIncidentById(UUID id) {
        Objects.requireNonNull(id, "id cannot be null");
        MDC.put("incidentId", id.toString());
        try {
            log.info("Fetching incident by id: [{}]", id);
            return incidentRepository.findById(IncidentId.of(id))
                    .map(IncidentResponseDto::fromDomain)
                    .orElseThrow(() -> new NoSuchElementException("Incident not found with ID: " + id));
        } finally {
            MDC.remove("incidentId");
        }
    }
}
