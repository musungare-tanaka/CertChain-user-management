package com.austin.msu_cert.dto;

import com.austin.msu_cert.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

// ── Request DTOs ────────────────────────────────────────────────────────────

public class UserDto {

    /** Register a new account */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
        )
        private String password;

        @Size(max = 100, message = "Full name must be at most 100 characters")
        private String fullName;
    }

    /** Login */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Username or email is required")
        private String usernameOrEmail;

        @NotBlank(message = "Password is required")
        private String password;
    }

    /** Update own profile */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {

        @Size(max = 100, message = "Full name must be at most 100 characters")
        private String fullName;

        @Email(message = "Invalid email format")
        private String email;

        /** If provided, the current password must also be supplied */
        @Size(min = 8, message = "New password must be at least 8 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "New password must contain at least one uppercase letter, one lowercase letter, and one digit"
        )
        private String newPassword;

        private String currentPassword;
    }

    // ── Response DTOs ────────────────────────────────────────────────────────

    /** Returned after a successful login */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String accessToken;
        private String tokenType;
        private Long expiresIn;
        private UserResponse user;
    }

    /** Public user info */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }

    /** Generic API response wrapper */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public static ApiResponse ok(String message, Object data) {
            return ApiResponse.builder().success(true).message(message).data(data).build();
        }

        public static ApiResponse ok(String message) {
            return ok(message, null);
        }

        public static ApiResponse error(String message) {
            return ApiResponse.builder().success(false).message(message).build();
        }
    }
}