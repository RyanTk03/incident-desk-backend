package com.example.incidents.dto;

import lombok.Data;

// pas de champ role : le role est toujours EMPLOYEE a l'inscription
@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
}
