package com.austin.msu_cert.service;

import com.austin.msu_cert.config.JwtUtil;
import com.austin.msu_cert.dto.*;
import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.InstitutionStatus;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.repository.InstitutionRepository;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final EmailNotificationService emailNotificationService;

    // ── Student Registration ──────────────────────────────────────────────────

    @Transactional
    public AuthResponse registerStudent(StudentRegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        if (userRepository.existsByStudentId(req.getStudentId())) {
            throw new BadRequestException("Student ID already registered");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .username(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.STUDENT)
                .studentId(req.getStudentId())
                .build();

        userRepository.save(user);
        auditLogService.log(
                user.getEmail(),
                user.getRole().name(),
                "REGISTER_STUDENT",
                "USER",
                String.valueOf(user.getId()),
                "Student account registered"
        );
        emailNotificationService.sendStudentAccountCreated(
                user.getEmail(),
                user.getFullName(),
                user.getStudentId()
        );
        return buildAuthResponse(user);
    }

    // ── Institution Registration ──────────────────────────────────────────────

    @Transactional
    public InstitutionResponse registerInstitution(InstitutionRegisterRequest req) {
        if (institutionRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Institution email already in use");
        }
        if (institutionRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
            throw new BadRequestException("Registration number already in use");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        String institutionId = UUID.randomUUID().toString();

        // Create Institution record
        Institution institution = Institution.builder()
                .id(institutionId)
                .name(req.getName())
                .registrationNumber(req.getRegistrationNumber())
                .email(req.getEmail())
                .contactPerson(req.getContactPerson())
                .phone(req.getPhone())
                .status(InstitutionStatus.PENDING)
                .build();

        institutionRepository.save(institution);

        // Create User account for the institution
        User user = User.builder()
                .fullName(req.getName())
                .email(req.getEmail())
                .username(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.INSTITUTION)
                .institutionId(institutionId)
                .enabled(false) // disabled until admin approves
                .build();

        userRepository.save(user);
        auditLogService.log(
                user.getEmail(),
                user.getRole().name(),
                "REGISTER_INSTITUTION",
                "INSTITUTION",
                institutionId,
                "Institution registered and awaiting admin approval"
        );
        emailNotificationService.sendInstitutionAccountCreated(
                institution.getEmail(),
                institution.getName(),
                institution.getRegistrationNumber()
        );

        return mapToInstitutionResponse(institution);
    }

    // ── Login (all roles) ─────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        auditLogService.log(
                user.getEmail(),
                user.getRole().name(),
                "LOGIN_SUCCESS",
                "USER",
                String.valueOf(user.getId()),
                "User successfully logged in"
        );
        return buildAuthResponse(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());

        String entityId = switch (user.getRole()) {
            case STUDENT -> user.getStudentId();
            case INSTITUTION -> user.getInstitutionId();
            default -> null;
        };

        if (entityId != null) claims.put("entityId", entityId);

        String token = jwtUtil.generateToken(claims, user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .user(AuthResponse.UserProfile.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .entityId(entityId)
                        .build())
                .build();
    }

    private InstitutionResponse mapToInstitutionResponse(Institution inst) {
        return InstitutionResponse.builder()
                .id(inst.getId())
                .name(inst.getName())
                .registrationNumber(inst.getRegistrationNumber())
                .email(inst.getEmail())
                .contactPerson(inst.getContactPerson())
                .phone(inst.getPhone())
                .status(inst.getStatus().name())
                .createdAt(inst.getCreatedAt())
                .approvedAt(inst.getApprovedAt())
                .build();
    }
}
