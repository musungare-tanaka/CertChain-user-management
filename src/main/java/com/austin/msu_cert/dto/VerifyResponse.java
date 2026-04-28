package com.austin.msu_cert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResponse {
    private boolean valid;
    private String certId;
    private String status;
    private String institution;
    private String student;
    private String course;
    private String ipfsCID;
    private boolean hashMatched;
    private boolean signatureValid;
    private long issuedAt;
    /** Human-readable result message */
    private String message;
}
