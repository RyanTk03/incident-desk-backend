package com.example.incidents.service;

import com.example.incidents.dto.HistoryResponse;
import com.example.incidents.dto.IncidentRequest;
import com.example.incidents.dto.IncidentResponse;
import com.example.incidents.entity.Category;
import com.example.incidents.entity.Incident;
import com.example.incidents.entity.IncidentHistory;
import com.example.incidents.entity.Role;
import com.example.incidents.entity.Status;
import com.example.incidents.entity.User;
import com.example.incidents.exception.BadRequestException;
import com.example.incidents.exception.ResourceNotFoundException;
import com.example.incidents.repository.IncidentHistoryRepository;
import com.example.incidents.repository.IncidentRepository;
import com.example.incidents.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final IncidentHistoryRepository historyRepository;

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

        incident = incidentRepository.save(incident);
        addHistory(incident, "Incident cree", null, Status.OPEN, currentUser);
        return IncidentResponse.from(incident);
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

        Status oldStatus = incident.getStatus();
        incident.setStatus(newStatus);
        incidentRepository.save(incident);

        addHistory(incident, oldStatus + " -> " + newStatus, oldStatus, newStatus, user);
        return IncidentResponse.from(incident);
    }

    // ---------- ADMIN ----------

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAll() {
        return incidentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    public IncidentResponse assign(Long incidentId, Long technicianId) {
        Incident incident = findIncident(incidentId);
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable"));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("L'utilisateur choisi n'est pas un technicien");
        }
        if (!technician.isEnabled()) {
            throw new BadRequestException("Ce technicien est desactive");
        }
        if (incident.getStatus() == Status.CLOSED) {
            throw new BadRequestException("Impossible d'affecter un incident ferme");
        }

        Status oldStatus = incident.getStatus();
        incident.setAssignedTo(technician);
        if (incident.getStatus() == Status.OPEN) {
            incident.setStatus(Status.ASSIGNED);
        }
        incidentRepository.save(incident);

        addHistory(incident, "Affecte a " + technician.getFullName(), oldStatus, incident.getStatus(),
                userService.getCurrentUser());
        return IncidentResponse.from(incident);
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> getHistory(Long incidentId) {
        Incident incident = findIncident(incidentId);
        checkCanView(incident, userService.getCurrentUser());
        return historyRepository.findByIncidentOrderByCreatedAtAsc(incident)
                .stream()
                .map(HistoryResponse::from)
                .toList();
    }

    // ---------- methodes utilitaires ----------

    private void addHistory(Incident incident, String description, Status oldStatus, Status newStatus, User user) {
        IncidentHistory history = IncidentHistory.builder()
                .incident(incident)
                .description(description)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .performedBy(user)
                .build();
        historyRepository.save(history);
    }

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
