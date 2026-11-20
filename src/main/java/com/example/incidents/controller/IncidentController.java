package com.example.incidents.controller;

import com.example.incidents.dto.IncidentRequest;
import com.example.incidents.dto.IncidentResponse;
import com.example.incidents.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<IncidentResponse> getMyIncidents() {
        return incidentService.getMyIncidents();
    }

    // le controle proprietaire / technicien affecte est fait dans le service
    @GetMapping("/{id}")
    public IncidentResponse getById(@PathVariable Long id) {
        return incidentService.getById(id);
    }
}
