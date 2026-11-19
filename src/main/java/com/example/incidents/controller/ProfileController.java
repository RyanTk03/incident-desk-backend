package com.example.incidents.controller;

import com.example.incidents.dto.ProfileUpdateRequest;
import com.example.incidents.dto.UserResponse;
import com.example.incidents.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public UserResponse getProfile() {
        return userService.getProfile();
    }

    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(request);
    }
}
