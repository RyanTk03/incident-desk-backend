package com.example.incidents.dto;

import com.example.incidents.entity.IncidentHistory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private String description;
    private String oldStatus;
    private String newStatus;
    private String performedBy;
    private LocalDateTime createdAt;

    public static HistoryResponse from(IncidentHistory h) {
        return new HistoryResponse(
                h.getId(),
                h.getDescription(),
                h.getOldStatus() != null ? h.getOldStatus().name() : null,
                h.getNewStatus() != null ? h.getNewStatus().name() : null,
                h.getPerformedBy() != null ? h.getPerformedBy().getFullName() : null,
                h.getCreatedAt()
        );
    }
}
