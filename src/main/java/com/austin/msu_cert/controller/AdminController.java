package com.austin.msu_cert.controller;

import com.austin.msu_cert.blockchain.CertificateService;
import com.austin.msu_cert.dto.AdminInstitutionCreateRequest;
import com.austin.msu_cert.dto.AdminInstitutionUpdateRequest;
import com.austin.msu_cert.dto.AuditLogResponse;
import com.austin.msu_cert.dto.ApiResponse;
import com.austin.msu_cert.dto.CertificateResponse;
import com.austin.msu_cert.dto.InstitutionResponse;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.InstitutionStatus;
import com.austin.msu_cert.service.AuditLogService;
import com.austin.msu_cert.service.EmailNotificationService;
import com.austin.msu_cert.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATOR')")
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

    /** POST /api/admin/institutions — create a new institution and institution user */
    @PostMapping("/institutions")
    public ResponseEntity<ApiResponse<InstitutionResponse>> createInstitution(
            @Valid @RequestBody AdminInstitutionCreateRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        InstitutionResponse response = institutionService.createInstitutionByAdmin(req);
        auditLogService.log(
                currentUser,
                "CREATE_INSTITUTION",
                "INSTITUTION",
                response.getId(),
                "Institution created by administrator"
        );
        emailNotificationService.sendInstitutionAccountCreated(
                response.getEmail(),
                response.getName(),
                response.getRegistrationNumber()
        );
        emailNotificationService.sendInstitutionVerificationStatus(
                response.getEmail(),
                response.getName(),
                response.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution created successfully", response));
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

    /** PUT /api/admin/institutions/{id} — update institution details */
    @PutMapping("/institutions/{id}")
    public ResponseEntity<ApiResponse<InstitutionResponse>> updateInstitution(
            @PathVariable String id,
            @Valid @RequestBody AdminInstitutionUpdateRequest req,
            @AuthenticationPrincipal User currentUser
    ) {
        InstitutionResponse response = institutionService.updateInstitutionByAdmin(id, req);
        auditLogService.log(
                currentUser,
                "UPDATE_INSTITUTION",
                "INSTITUTION",
                id,
                "Institution details updated by administrator"
        );
        emailNotificationService.sendInstitutionVerificationStatus(
                response.getEmail(),
                response.getName(),
                response.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution updated successfully.", response));
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

    /** POST /api/admin/institutions/{id}/disable */
    @PostMapping("/institutions/{id}/disable")
    public ResponseEntity<ApiResponse<InstitutionResponse>> disable(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        InstitutionResponse response = institutionService.disableInstitution(id);
        auditLogService.log(
                currentUser,
                "DISABLE_INSTITUTION",
                "INSTITUTION",
                id,
                "Institution disabled by administrator"
        );
        emailNotificationService.sendInstitutionVerificationStatus(
                response.getEmail(),
                response.getName(),
                response.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution disabled.", response));
    }

    /** DELETE /api/admin/institutions/{id} */
    @DeleteMapping("/institutions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInstitution(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        institutionService.deleteInstitutionByAdmin(id);
        auditLogService.log(
                currentUser,
                "DELETE_INSTITUTION",
                "INSTITUTION",
                id,
                "Institution deleted by administrator"
        );
        return ResponseEntity.ok(ApiResponse.ok("Institution deleted.", null));
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
