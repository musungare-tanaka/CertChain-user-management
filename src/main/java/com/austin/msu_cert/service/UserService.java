package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.UserDto;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public List<UserDto.UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto.UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return UserDto.UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserById(Long id, String currentEmail) {
        User targetUser = findUserById(id);
        ensureOwnerOrAdmin(targetUser, currentEmail);
        return UserDto.UserResponse.from(targetUser);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return UserDto.UserResponse.from(user);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    public UserDto.UserResponse updateUser(Long id, UserDto.UpdateRequest request, String currentUsername) {
        User user = findUserById(id);

        // Only the account owner or an admin may update
        ensureOwnerOrAdmin(user, currentUsername);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getNewPassword() != null) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User saved = userRepository.save(user);
        log.info("User {} updated by {}", id, currentUsername);
        return UserDto.UserResponse.from(saved);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void deleteUser(Long id, String currentUsername) {
        User user = findUserById(id);
        ensureOwnerOrAdmin(user, currentUsername);
        userRepository.delete(user);
        log.info("User {} deleted by {}", id, currentUsername);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    private void ensureOwnerOrAdmin(User targetUser, String currentUsername) {
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isOwner = targetUser.getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this account");
        }
    }
}
