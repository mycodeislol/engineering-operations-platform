package com.eop.operations.infrastructure.web;

import com.eop.operations.application.GetIncidentsService;
import com.eop.operations.infrastructure.web.dto.IncidentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/incidents")
@CrossOrigin(origins = "*")
public class IncidentController {

    private final GetIncidentsService getIncidentsService;

    public IncidentController(GetIncidentsService getIncidentsService) {
        this.getIncidentsService = getIncidentsService;
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getIncidents(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) UUID envId,
            @RequestParam(required = false) String severity) {
        
        return ResponseEntity.ok(getIncidentsService.execute(serviceId, envId, severity));
    }
}
