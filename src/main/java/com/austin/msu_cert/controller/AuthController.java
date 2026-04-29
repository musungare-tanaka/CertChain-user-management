package com.austin.msu_cert.controller;

import com.austin.msu_cert.dto.*;
import com.austin.msu_cert.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register/student
     * Registers a new student. Account is immediately active.
     */
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(
            @Valid @RequestBody StudentRegisterRequest req
    ) {
        AuthResponse response = authService.registerStudent(req);
        return ResponseEntity.ok(ApiResponse.ok("Student registered successfully", response));
    }

    /**
     * POST /api/auth/register/institution
     * Legacy endpoint maintained for backward compatibility.
     * Institution onboarding is administrator-managed.
     */
    @PostMapping("/register/institution")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<InstitutionResponse>> registerInstitution(
            @Valid @RequestBody InstitutionRegisterRequest req
    ) {
        InstitutionResponse response = authService.registerInstitution(req);
        return ResponseEntity.ok(ApiResponse.ok(
                "Institution registered by administrator.",
                response
        ));
    }

    /**
     * POST /api/auth/login
     * Works for STUDENT, INSTITUTION, ADMIN, and ADMINISTRATOR roles.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req
    ) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
