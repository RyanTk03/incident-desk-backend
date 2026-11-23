package com.example.incidents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;        // access token (JWT, courte duree)
    private String refreshToken; // pour obtenir un nouvel access token
    private String role;
    private String fullName;
}
