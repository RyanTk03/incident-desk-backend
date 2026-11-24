package com.example.incidents.service;

import com.example.incidents.dto.ChangePasswordRequest;
import com.example.incidents.dto.ProfileUpdateRequest;
import com.example.incidents.dto.UserResponse;
import com.example.incidents.entity.User;
import com.example.incidents.exception.BadRequestException;
import com.example.incidents.exception.EmailAlreadyUsedException;
import com.example.incidents.exception.ResourceNotFoundException;
import com.example.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Recupere l'utilisateur connecte a partir du SecurityContext
     * (rempli par le JwtFilter, le "name" c'est l'email)
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public UserResponse getProfile() {
        return UserResponse.from(getCurrentUser());
    }

    public UserResponse updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUser();

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }
        // NB: si l'email change, l'ancien JWT (subject = ancien email) ne marche plus,
        // le front passe alors par /auth/refresh pour en avoir un nouveau

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        return UserResponse.from(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // on verifie l'ancien mot de passe avant tout
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("La confirmation ne correspond pas au nouveau mot de passe");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
