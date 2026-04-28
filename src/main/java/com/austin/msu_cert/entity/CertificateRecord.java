package com.austin.msu_cert.entity;

import com.austin.msu_cert.enums.CertStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Off-chain mirror of every certificate stored on the blockchain.
 * The source of truth for validity is always the blockchain —
 * this record exists for fast querying, listing, and sharing.
 */
@Entity
@Table(name = "certificate_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRecord {

    /** Primary key — same certId that lives on the blockchain. */
    @Id
    private String certId;

    @Column(nullable = false)
    private String institutionId;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String courseName;

    /** IPFS CID where the PDF is stored on Pinata. */
    @Column(nullable = false)
    private String ipfsCID;

    /** Hex-encoded SHA-256 hash of the PDF — the same bytes32 stored on chain. */
    @Column(nullable = false)
    private String documentHash;

    /** Base64-encoded digital signature for the document hash. */
    @Column(nullable = false, length = 2048)
    private String digitalSignature;

    /** Signature algorithm used for `digitalSignature`. */
    @Builder.Default
    @Column(nullable = false)
    private String signatureAlgorithm = "SHA256withRSA";

    /** Ethereum transaction hash of the issueCertificate() call. */
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CertStatus status = CertStatus.ACTIVE;

    @Builder.Default
    @Column(name = "issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}
