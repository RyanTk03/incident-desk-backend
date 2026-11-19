package com.example.incidents.service;

import com.example.incidents.dto.ProfileUpdateRequest;
import com.example.incidents.dto.UserResponse;
import com.example.incidents.entity.User;
import com.example.incidents.exception.ResourceNotFoundException;
import com.example.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        return UserResponse.from(userRepository.save(user));
    }
}
