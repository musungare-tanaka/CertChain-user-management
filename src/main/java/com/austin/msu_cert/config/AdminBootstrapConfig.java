package com.austin.msu_cert.config;

import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap-enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.admin.email:admin@msu.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.admin.full-name:System Administrator}")
    private String adminFullName;

    @Bean
    CommandLineRunner ensureAdminAccount() {
        return args -> {
            if (!bootstrapEnabled) {
                log.info("Admin bootstrap is disabled");
                return;
            }

            if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                log.warn("Admin bootstrap skipped: missing admin email or password configuration");
                return;
            }

            var existingByEmail = userRepository.findByEmail(adminEmail);
            if (existingByEmail.isPresent()) {
                User existing = existingByEmail.get();
                if (existing.getRole() == User.Role.ADMIN) {
                    if (!existing.isEnabled()) {
                        existing.setEnabled(true);
                        userRepository.save(existing);
                    }
                    log.info("Admin account already exists: {}", adminEmail);
                } else {
                    log.warn("Admin bootstrap skipped: email {} already exists with role {}", adminEmail, existing.getRole());
                }
                return;
            }

            if (userRepository.existsByRole(User.Role.ADMIN)) {
                log.info("Admin bootstrap skipped: an admin account already exists");
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .username(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .fullName(adminFullName)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            log.warn("Created bootstrap admin account: {} (change ADMIN_PASSWORD in production)", adminEmail);
        };
    }
}
