package com.example.incidents.service;

import com.example.incidents.dto.IncidentRequest;
import com.example.incidents.dto.IncidentResponse;
import com.example.incidents.entity.Category;
import com.example.incidents.entity.Incident;
import com.example.incidents.entity.Role;
import com.example.incidents.entity.Status;
import com.example.incidents.entity.User;
import com.example.incidents.exception.ResourceNotFoundException;
import com.example.incidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        Incident incident = findIncident(id);
        checkCanView(incident, userService.getCurrentUser());
        return IncidentResponse.from(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAssignedIncidents() {
        User technician = userService.getCurrentUser();
        return incidentRepository.findByAssignedToOrderByCreatedAtDesc(technician)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    public IncidentResponse updateStatus(Long id, Status newStatus) {
        Incident incident = findIncident(id);
        User user = userService.getCurrentUser();

        // un technicien ne peut modifier que les incidents qui lui sont affectes
        if (user.getRole() == Role.TECHNICIAN && !isAssignedTo(incident, user)) {
            throw new AccessDeniedException("Cet incident ne vous est pas affecte");
        }

        incident.setStatus(newStatus);
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    // ---------- methodes utilitaires ----------

    private Incident findIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable"));
    }

    /**
     * Verification cote backend (masquer le bouton dans React ne suffit pas) :
     * - ADMIN : tout
     * - EMPLOYEE : seulement ses incidents
     * - TECHNICIAN : seulement les incidents qui lui sont affectes
     */
    private void checkCanView(Incident incident, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.EMPLOYEE && incident.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        if (user.getRole() == Role.TECHNICIAN && isAssignedTo(incident, user)) {
            return;
        }
        throw new AccessDeniedException("Vous n'avez pas acces a cet incident");
    }

    private boolean isAssignedTo(Incident incident, User technician) {
        return incident.getAssignedTo() != null && incident.getAssignedTo().getId().equals(technician.getId());
    }
}
