package com.example.incidents.service;

import com.example.incidents.dto.StatisticsResponse;
import com.example.incidents.dto.UserResponse;
import com.example.incidents.entity.Role;
import com.example.incidents.entity.Status;
import com.example.incidents.entity.User;
import com.example.incidents.exception.BadRequestException;
import com.example.incidents.exception.ResourceNotFoundException;
import com.example.incidents.repository.IncidentRepository;
import com.example.incidents.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (user.getRole() == Role.ADMIN && !enabled) {
            throw new BadRequestException("Impossible de desactiver un administrateur");
        }

        user.setEnabled(enabled);
        return UserResponse.from(userRepository.save(user));
    }

    public StatisticsResponse getStatistics() {
        return StatisticsResponse.builder()
                .totalUsers(userRepository.count())
                .employees(userRepository.countByRole(Role.EMPLOYEE))
                .technicians(userRepository.countByRole(Role.TECHNICIAN))
                .totalIncidents(incidentRepository.count())
                .openIncidents(incidentRepository.countByStatus(Status.OPEN))
                .inProgressIncidents(incidentRepository.countByStatus(Status.IN_PROGRESS))
                .closedIncidents(incidentRepository.countByStatus(Status.CLOSED))
                .build();
    }
}
