package com.austin.msu_cert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private String certId;
    private String institutionId;
    private String institutionName;
    private String studentId;
    private String studentName;
    private String courseName;
    private String ipfsCID;
    private String ipfsUrl;
    private String documentHash;
    private String digitalSignature;
    private String signatureAlgorithm;
    private String txHash;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime revokedAt;
}
