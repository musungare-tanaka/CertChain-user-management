package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.CertificateResponse;
import com.austin.msu_cert.dto.ShareResponse;
import com.austin.msu_cert.entity.ShareToken;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.exceptions.ResourceNotFoundException;
import com.austin.msu_cert.repository.CertificateRecordRepository;
import com.austin.msu_cert.repository.ShareTokenRepository;
import com.austin.msu_cert.blockchain.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareTokenRepository shareTokenRepository;
    private final CertificateRecordRepository certificateRecordRepository;
    private final CertificateService certificateService;

    @Value("${app.share-base-url}")
    private String shareBaseUrl;

    /**
     * Generates a shareable token for a certificate.
     * Only the student who owns the certificate can generate a share link.
     *
     * @param certId        the certificate to share
     * @param studentUserId the requesting student's user ID (as string)
     * @param studentId     the student's academic ID stored in the cert
     * @param expiryDays    null = never expires, otherwise days until expiry
     */
    @Transactional
    public ShareResponse generateShareLink(
            String certId,
            String studentUserId,
            String studentId,
            Integer expiryDays
    ) {
        // Ensure certificate exists and belongs to this student
        var record = certificateRecordRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certId));

        if (!record.getStudentId().equals(studentId)) {
            throw new BadRequestException("You do not own this certificate");
        }

        LocalDateTime expiresAt = expiryDays != null
                ? LocalDateTime.now().plusDays(expiryDays)
                : null;

        String token = UUID.randomUUID().toString();

        ShareToken shareToken = ShareToken.builder()
                .token(token)
                .certId(certId)
                .studentUserId(studentUserId)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        shareTokenRepository.save(shareToken);

        return ShareResponse.builder()
                .token(token)
                .shareUrl(shareBaseUrl + "/" + token)
                .certId(certId)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Resolves a share token and returns the certificate details.
     * This endpoint is public — no auth required.
     */
    public CertificateResponse resolveShareToken(String token) {
        ShareToken shareToken = resolveActiveToken(token);
        return certificateService.getCertificate(shareToken.getCertId());
    }

    public String resolveShareTokenToCertId(String token) {
        ShareToken shareToken = resolveActiveToken(token);
        return shareToken.getCertId();
    }

    private ShareToken resolveActiveToken(String token) {
        ShareToken shareToken = shareTokenRepository.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share link is invalid or has been revoked"));

        if (shareToken.getExpiresAt() != null
                && shareToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This share link has expired");
        }
        return shareToken;
    }

    /**
     * Revokes a share token — student can invalidate links they've shared.
     */
    @Transactional
    public void revokeShareToken(String token, String studentUserId) {
        ShareToken shareToken = shareTokenRepository.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Share token not found"));

        if (!shareToken.getStudentUserId().equals(studentUserId)) {
            throw new BadRequestException("You do not own this share link");
        }

        shareToken.setActive(false);
        shareTokenRepository.save(shareToken);
    }
}
