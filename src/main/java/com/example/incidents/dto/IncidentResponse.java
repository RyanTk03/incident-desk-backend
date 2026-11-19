package com.example.incidents.dto;

import com.example.incidents.entity.Incident;
import com.example.incidents.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary createdBy;
    private UserSummary assignedTo;

    // version reduite de l'utilisateur (pas besoin de tout renvoyer)
    @Data
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String email;

        static UserSummary from(User u) {
            if (u == null) return null;
            return new UserSummary(u.getId(), u.getFullName(), u.getEmail());
        }
    }

    public static IncidentResponse from(Incident i) {
        return new IncidentResponse(
                i.getId(),
                i.getTitle(),
                i.getDescription(),
                i.getCategory() != null ? i.getCategory().name() : null,
                i.getPriority().name(),
                i.getStatus().name(),
                i.getCreatedAt(),
                i.getUpdatedAt(),
                UserSummary.from(i.getCreatedBy()),
                UserSummary.from(i.getAssignedTo())
        );
    }
}
