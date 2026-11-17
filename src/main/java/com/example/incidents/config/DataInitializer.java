package com.example.incidents.config;

import com.example.incidents.entity.Role;
import com.example.incidents.entity.User;
import com.example.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree les comptes de test au demarrage (si la base est vide).
 * Mot de passe pour tous : 123456
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        createUser("Ahmed Benali", "employee1@test.com", Role.EMPLOYEE);
        createUser("Sara Idrissi", "employee2@test.com", Role.EMPLOYEE);
        createUser("Youssef Tazi", "tech@test.com", Role.TECHNICIAN);
        createUser("Karim Alaoui", "tech2@test.com", Role.TECHNICIAN);
        createUser("Admin", "admin@test.com", Role.ADMIN);

        System.out.println(">>> Comptes de test crees (mot de passe : 123456)");
    }

    private void createUser(String fullName, String email, Role role) {
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode("123456"))
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);
    }
}
