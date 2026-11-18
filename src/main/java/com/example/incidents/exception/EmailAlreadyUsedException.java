package com.example.incidents.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("Email deja utilise : " + email);
    }
}
