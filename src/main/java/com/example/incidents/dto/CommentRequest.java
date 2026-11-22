package com.example.incidents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "La note ne peut pas etre vide")
    @Size(max = 1000)
    private String content;
}
