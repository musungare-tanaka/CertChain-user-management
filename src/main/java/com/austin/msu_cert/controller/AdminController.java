package com.austin.msu_cert.controller;

import com.austin.msu_cert.blockchain.CertificateService;
import com.austin.msu_cert.dto.AuditLogResponse;
import com.austin.msu_cert.dto.ApiResponse;
import com.austin.msu_cert.dto.CertificateResponse;
import com.austin.msu_cert.dto.InstitutionResponse;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.InstitutionStatus;
import com.austin.msu_cert.service.AuditLogService;
import com.austin.msu_cert.service.EmailNotificationService;
import com.austin.msu_cert.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final InstitutionService institutionService;
    private final CertificateService certificateService;
    private final AuditLogService auditLogService;
    private final EmailNotificationService emailNotificationService;

    /** GET /api/admin/institutions — list all institutions */
    @GetMapping("/institutions")
    public ResponseEntity<ApiResponse<List<InstitutionResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.getAllInstitutions()));
    }

    /** GET /api/admin/certificates — list all certificates */
    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> listCertificates() {
        return ResponseEntity.ok(ApiResponse.ok(certificateService.getAllCertificates()));
    }

    /** GET /api/admin/institutions/pending — list pending institutions */
    @GetMapping("/institutions/pending")
    public ResponseEntity<ApiResponse<List<InstitutionResponse>>> listPending() {
        return ResponseEntity.ok(ApiResponse.ok(
                institutionService.getByStatus(InstitutionStatus.PENDING)
        ));
    }

    /** GET /api/admin/institutions/{id} — get institution detail */
    @GetMapping("/institutions/{id}")
    public ResponseEntity<ApiResponse<InstitutionResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.getById(id)));
    }

    /** POST /api/admin/institutions/{id}/approve */
    @PostMapping("/institutions/{id}/approve")
    public ResponseEntity<ApiResponse<InstitutionResponse>> approve(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        InstitutionResponse response = institutionService.approveInstitution(id);
        auditLogService.log(
                currentUser,
                "APPROVE_INSTITUTION",
                "INSTITUTION",
                id,
                "Institution approved by admin"
        );
        emailNotificationService.sendInstitutionVerificationStatus(
                response.getEmail(),
                response.getName(),
                response.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution approved. Login is now enabled.", response));
    }

    /** POST /api/admin/institutions/{id}/suspend */
    @PostMapping("/institutions/{id}/suspend")
    public ResponseEntity<ApiResponse<InstitutionResponse>> suspend(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        InstitutionResponse response = institutionService.suspendInstitution(id);
        auditLogService.log(
                currentUser,
                "SUSPEND_INSTITUTION",
                "INSTITUTION",
                id,
                "Institution suspended by admin"
        );
        emailNotificationService.sendInstitutionVerificationStatus(
                response.getEmail(),
                response.getName(),
                response.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution suspended.", response));
    }

    /** GET /api/admin/audit-logs?limit=100 */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> listAuditLogs(
            @RequestParam(defaultValue = "100") int limit
    ) {
        int cappedLimit = Math.max(1, Math.min(limit, 200));
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getRecent(cappedLimit)));
    }
}
