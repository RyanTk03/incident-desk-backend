package com.example.incidents.controller;

import com.example.incidents.dto.StatisticsResponse;
import com.example.incidents.dto.UserResponse;
import com.example.incidents.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// /api/admin/** est deja reserve a ADMIN dans SecurityConfig,
// le @PreAuthorize c'est une securite en plus
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return adminService.getAllUsers();
    }

    @PutMapping("/users/{id}/enable")
    public UserResponse enable(@PathVariable Long id) {
        return adminService.setEnabled(id, true);
    }

    @PutMapping("/users/{id}/disable")
    public UserResponse disable(@PathVariable Long id) {
        return adminService.setEnabled(id, false);
    }

    @GetMapping("/statistics")
    public StatisticsResponse getStatistics() {
        return adminService.getStatistics();
    }
}
