package com.eop.operations.infrastructure.web;

import com.eop.operations.infrastructure.web.dto.IncidentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations/incidents")
public class IncidentController {

    // TODO: Wire application query use case port here
    public IncidentController() {}

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getIncidents(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) UUID envId,
            @RequestParam(required = false) String severity) {
        
        // Placeholder returning filtered/empty collection bound to hexagonal contract
        return ResponseEntity.ok(List.of());
    }
}
