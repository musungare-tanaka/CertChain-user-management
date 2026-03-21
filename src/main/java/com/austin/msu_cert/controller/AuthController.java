package com.austin.msu_cert.controller;

import com.austin.msu_cert.dto.UserDto;
import com.austin.msu_cert.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new user account. Returns a JWT on success.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto.ApiResponse> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.AuthResponse auth = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserDto.ApiResponse.ok("Account created successfully", auth));
    }

    /**
     * POST /api/auth/login
     * Authenticate and receive a JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto.ApiResponse> login(@Valid @RequestBody UserDto.LoginRequest request) {
        UserDto.AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(UserDto.ApiResponse.ok("Login successful", auth));
    }
}