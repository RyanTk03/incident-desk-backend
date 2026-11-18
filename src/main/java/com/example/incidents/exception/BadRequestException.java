package com.example.incidents.exception;

// pour les regles metier non respectees (ex: incident ferme)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
