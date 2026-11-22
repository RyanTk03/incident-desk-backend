package com.example.incidents.dto;

import com.example.incidents.entity.IncidentComment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;

    public static CommentResponse from(IncidentComment c) {
        return new CommentResponse(c.getId(), c.getContent(), c.getAuthor().getFullName(), c.getCreatedAt());
    }
}
