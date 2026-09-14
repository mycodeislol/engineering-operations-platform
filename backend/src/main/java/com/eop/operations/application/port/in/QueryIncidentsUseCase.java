package com.eop.operations.application.port.in;

import com.eop.operations.application.dto.IncidentQueryCriteria;
import com.eop.operations.application.dto.IncidentResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface QueryIncidentsUseCase {
    List<IncidentResponseDto> queryIncidents(@NotNull @Valid IncidentQueryCriteria criteria);
    IncidentResponseDto getIncidentById(@NotNull UUID id);
}
