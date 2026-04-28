package com.austin.msu_cert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A one-time or persistent shareable token that lets anyone (e.g. employers)
 * view a specific certificate without needing an account.
 */
@Entity
@Table(name = "share_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareToken {

    @Id
    private String token; // UUID

    @Column(nullable = false)
    private String certId;

    /** The student who generated this share link. */
    @Column(nullable = false)
    private String studentUserId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Null means never expires. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean active = true;
}