package com.austin.msu_cert.controller;

import com.austin.msu_cert.dto.*;
import com.austin.msu_cert.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * Registers an institution. Account is PENDING until admin approves.
     */
    @PostMapping("/register/institution")
    public ResponseEntity<ApiResponse<InstitutionResponse>> registerInstitution(
            @Valid @RequestBody InstitutionRegisterRequest req
    ) {
        InstitutionResponse response = authService.registerInstitution(req);
        return ResponseEntity.ok(ApiResponse.ok(
                "Institution registered. Awaiting admin approval before login is enabled.",
                response
        ));
    }

    /**
     * POST /api/auth/login
     * Works for STUDENT, INSTITUTION, and ADMIN roles.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req
    ) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}