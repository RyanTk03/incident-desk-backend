package com.example.incidents.service;

import com.example.incidents.dto.IncidentRequest;
import com.example.incidents.dto.IncidentResponse;
import com.example.incidents.entity.Category;
import com.example.incidents.entity.Incident;
import com.example.incidents.entity.Status;
import com.example.incidents.entity.User;
import com.example.incidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserService userService;

    public IncidentResponse create(IncidentRequest request) {
        User currentUser = userService.getCurrentUser();

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : Category.OTHER)
                .priority(request.getPriority())
                .status(Status.OPEN)
                .createdBy(currentUser)
                .build();

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getMyIncidents() {
        User currentUser = userService.getCurrentUser();
        return incidentRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }
}
