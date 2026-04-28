package com.austin.msu_cert.controller;

import com.austin.msu_cert.blockchain.CertificateService;
import com.austin.msu_cert.dto.*;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.service.AuditLogService;
import com.austin.msu_cert.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final ShareService shareService;
    private final AuditLogService auditLogService;

    // ── Institution: Issue certificate ───────────────────────────────────────

    /**
     * POST /api/certificates/issue
     * Multipart: "metadata" (JSON) + "pdf" (file)
     * Role: INSTITUTION
     */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse<CertificateResponse>> issueCertificate(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestPart("metadata") IssueCertificateRequest req,
            @RequestPart("pdf") MultipartFile pdf
    ) throws Exception {
        CertificateResponse response = certificateService.issueCertificate(
                currentUser.getInstitutionId(), req, pdf
        );
        auditLogService.log(
                currentUser,
                "ISSUE_CERTIFICATE",
                "CERTIFICATE",
                response.getCertId(),
                "Issued certificate for studentId=" + req.getStudentId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Certificate issued successfully", response));
    }

    // ── Institution: Revoke certificate ──────────────────────────────────────

    /**
     * DELETE /api/certificates/{certId}/revoke
     * Role: INSTITUTION
     */
    @DeleteMapping("/{certId}/revoke")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse<Void>> revokeCertificate(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String certId
    ) throws Exception {
        certificateService.revokeCertificate(certId, currentUser.getInstitutionId());
        auditLogService.log(
                currentUser,
                "REVOKE_CERTIFICATE",
                "CERTIFICATE",
                certId,
                "Certificate revoked by institution user"
        );
        return ResponseEntity.ok(ApiResponse.ok("Certificate revoked", null));
    }

    // ── Institution: list issued certificates ─────────────────────────────────

    /**
     * GET /api/certificates/institution
     * Role: INSTITUTION
     */
    @GetMapping("/institution")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getInstitutionCertificates(
            @AuthenticationPrincipal User currentUser
    ) {
        List<CertificateResponse> certs =
                certificateService.getCertificatesForInstitution(currentUser.getInstitutionId());
        return ResponseEntity.ok(ApiResponse.ok(certs));
    }

    // ── Student: list my certificates ─────────────────────────────────────────

    /**
     * GET /api/certificates/mine
     * Role: STUDENT
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getMyCertificates(
            @AuthenticationPrincipal User currentUser
    ) {
        List<CertificateResponse> certs =
                certificateService.getCertificatesForStudent(currentUser.getStudentId());
        return ResponseEntity.ok(ApiResponse.ok(certs));
    }

    // ── Student: generate share link ──────────────────────────────────────────

    /**
     * POST /api/certificates/{certId}/share?expiryDays=7
     * Role: STUDENT
     */
    @PostMapping("/{certId}/share")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ShareResponse>> generateShareLink(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String certId,
            @RequestParam(required = false) Integer expiryDays
    ) {
        ShareResponse share = shareService.generateShareLink(
                certId,
                String.valueOf(currentUser.getId()),
                currentUser.getStudentId(),
                expiryDays
        );
        auditLogService.log(
                currentUser,
                "GENERATE_SHARE_LINK",
                "CERTIFICATE",
                certId,
                "Generated share link token=" + share.getToken()
        );
        return ResponseEntity.ok(ApiResponse.ok("Share link generated", share));
    }

    // ── Student: revoke share link ────────────────────────────────────────────

    /**
     * DELETE /api/certificates/share/{token}
     * Role: STUDENT
     */
    @DeleteMapping("/share/{token}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> revokeShareLink(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String token
    ) {
        shareService.revokeShareToken(token, String.valueOf(currentUser.getId()));
        auditLogService.log(
                currentUser,
                "REVOKE_SHARE_LINK",
                "SHARE_TOKEN",
                token,
                "Share link revoked by student"
        );
        return ResponseEntity.ok(ApiResponse.ok("Share link revoked", null));
    }

    // ── Public: resolve share token ───────────────────────────────────────────

    /**
     * GET /api/certificates/share/{token}
     * Public — no auth required (employers use this)
     */
    @GetMapping("/share/{token}")
    public ResponseEntity<ApiResponse<CertificateResponse>> resolveShareToken(
            @PathVariable String token
    ) {
        CertificateResponse cert = shareService.resolveShareToken(token);
        return ResponseEntity.ok(ApiResponse.ok(cert));
    }

    // ── Public: Verify by cert ID ─────────────────────────────────────────────

    /**
     * GET /api/certificates/verify/{certId}
     * Public — no auth required
     */
    @GetMapping("/verify/{certId}")
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyById(
            @PathVariable String certId
    ) throws Exception {
        VerifyResponse result = certificateService.verifyById(certId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── Public: Verify by uploaded PDF ────────────────────────────────────────

    /**
     * POST /api/certificates/verify/document
     * Multipart: "pdf" (file)
     * Public — employers upload a certificate PDF to verify it
     */
    @PostMapping("/verify/document")
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyByDocument(
            @RequestPart("pdf") MultipartFile pdf
    ) throws Exception {
        VerifyResponse result = certificateService.verifyByDocument(pdf);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/certificates/{certId}/download
     * Authenticated owner/admin download endpoint.
     */
    @GetMapping("/{certId}/download")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable String certId,
            @AuthenticationPrincipal User currentUser
    ) {
        byte[] fileBytes = certificateService.downloadCertificateFile(certId, currentUser);
        String filename = certId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }

    /**
     * GET /api/certificates/share/{token}/download
     * Public download using a valid share token.
     */
    @GetMapping("/share/{token}/download")
    public ResponseEntity<byte[]> downloadSharedCertificate(@PathVariable String token) {
        String certId = shareService.resolveShareTokenToCertId(token);
        byte[] fileBytes = certificateService.downloadCertificateFile(certId);
        String filename = certId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }

    /**
     * GET /api/certificates/storage/{cid}
     * Internal/public gateway fallback for locally stored files in development.
     */
    @GetMapping("/storage/{cid}")
    public ResponseEntity<byte[]> downloadByCid(@PathVariable String cid) {
        byte[] fileBytes = certificateService.downloadCertificateFileByCid(cid);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }
}
