package com.austin.msu_cert.service;

import com.austin.msu_cert.config.JwtUtil;
import com.austin.msu_cert.dto.UserDto;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────────

    public UserDto.AuthResponse register(UserDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());

        String token = jwtUtil.generateToken(saved);
        return buildAuthResponse(token, saved);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    public UserDto.AuthResponse login(UserDto.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);
        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(token, user);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private UserDto.AuthResponse buildAuthResponse(String token, User user) {
        return UserDto.AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .user(UserDto.UserResponse.from(user))
                .build();
    }
}