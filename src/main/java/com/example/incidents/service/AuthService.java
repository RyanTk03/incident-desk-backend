package com.example.incidents.service;

import com.example.incidents.dto.AuthResponse;
import com.example.incidents.dto.LoginRequest;
import com.example.incidents.dto.RefreshRequest;
import com.example.incidents.dto.RegisterRequest;
import com.example.incidents.entity.RefreshToken;
import com.example.incidents.entity.Role;
import com.example.incidents.entity.User;
import com.example.incidents.exception.EmailAlreadyUsedException;
import com.example.incidents.exception.InvalidTokenException;
import com.example.incidents.repository.UserRepository;
import com.example.incidents.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE) // jamais choisi par l'utilisateur
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // leve BadCredentialsException si mauvais mot de passe
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(token, refreshToken.getToken(), user.getRole().name(), user.getFullName());
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.verify(request.getRefreshToken());
        User user = refreshToken.getUser();

        if (!user.isEnabled()) {
            refreshTokenService.delete(refreshToken.getToken());
            throw new InvalidTokenException("Compte desactive");
        }

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(newAccessToken, refreshToken.getToken(), user.getRole().name(), user.getFullName());
    }

    // au logout on supprime le refresh token -> il ne peut plus servir
    // (l'access token reste valide jusqu'a son expiration, d'ou la duree courte)
    public void logout(RefreshRequest request) {
        refreshTokenService.delete(request.getRefreshToken());
    }
}
