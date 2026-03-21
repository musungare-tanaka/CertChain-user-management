package com.austin.msu_cert.controller;

import com.austin.msu_cert.dto.UserDto;
import com.austin.msu_cert.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Admin: list all users ────────────────────────────────────────────────

    /**
     * GET /api/users
     * Requires ADMIN role.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto.ApiResponse> getAllUsers() {
        List<UserDto.UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(UserDto.ApiResponse.ok("Users retrieved", users));
    }

    // ── Get user by id ───────────────────────────────────────────────────────

    /**
     * GET /api/users/{id}
     * Authenticated users may only view their own profile; admins may view any.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UserDto.UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(UserDto.ApiResponse.ok("User retrieved", user));
    }

    // ── Get own profile ──────────────────────────────────────────────────────

    /**
     * GET /api/users/me
     * Returns the currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.ApiResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UserDto.UserResponse user = userService.getUserByUsername(currentUser.getUsername());
        return ResponseEntity.ok(UserDto.ApiResponse.ok("Profile retrieved", user));
    }

    // ── Update ───────────────────────────────────────────────────────────────

    /**
     * PUT /api/users/{id}
     * Account owners may update themselves; admins may update any account.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UserDto.UserResponse updated = userService.updateUser(id, request, currentUser.getUsername());
        return ResponseEntity.ok(UserDto.ApiResponse.ok("Account updated successfully", updated));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /**
     * DELETE /api/users/{id}
     * Account owners may delete their own account; admins may delete any account.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        userService.deleteUser(id, currentUser.getUsername());
        return ResponseEntity.ok(UserDto.ApiResponse.ok("Account deleted successfully"));
    }
}