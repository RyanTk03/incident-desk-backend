package com.example.incidents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// pas de champ role : le role est toujours EMPLOYEE a l'inscription
@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
    private String password;
}
