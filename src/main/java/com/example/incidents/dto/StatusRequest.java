package com.example.incidents.dto;

import com.example.incidents.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private Status status;
}
