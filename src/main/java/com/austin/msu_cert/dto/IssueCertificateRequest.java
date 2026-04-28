package com.austin.msu_cert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssueCertificateRequest {

    /**
     * The student's official ID (e.g. R123456X).
     * Must match the studentId registered on the student's account.
     */
    @NotBlank
    private String studentId;

    @NotBlank
    private String studentName;

    @NotBlank
    private String courseName;

    /**
     * Optional: override the certId. If blank, the backend generates a UUID.
     */
    private String certId;
}