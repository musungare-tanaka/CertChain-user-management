package com.austin.msu_cert.blockchain;

import com.austin.msu_cert.dto.CertificateResponse;
import com.austin.msu_cert.dto.IssueCertificateRequest;
import com.austin.msu_cert.dto.VerifyResponse;
import com.austin.msu_cert.entity.CertificateRecord;
import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.CertStatus;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.exceptions.ForbiddenException;
import com.austin.msu_cert.exceptions.ResourceNotFoundException;
import com.austin.msu_cert.repository.CertificateRecordRepository;
import com.austin.msu_cert.repository.InstitutionRepository;
import com.austin.msu_cert.repository.UserRepository;
import com.austin.msu_cert.service.DigitalSignatureService;
import com.austin.msu_cert.service.EmailNotificationService;
import com.certchain.blockchain.CertificateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final long MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024;

    private final CertificateRegistry registry;
    private final PinataService pinataService;
    private final CertificateRecordRepository certificateRecordRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final DigitalSignatureService digitalSignatureService;
    private final EmailNotificationService emailNotificationService;

    // ── Issue Certificate ─────────────────────────────────────────────────────

    @Transactional
    public CertificateResponse issueCertificate(
            String institutionId,
            IssueCertificateRequest req,
            MultipartFile pdf
    ) throws Exception {
        byte[] pdfBytes = validateAndReadPdf(pdf);

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));
        User student = userRepository.findByStudentId(req.getStudentId())
                .orElseThrow(() -> new BadRequestException(
                        "Student account not found for ID " + req.getStudentId() + ". The student must be registered."
                ));

        // 1. Hash the PDF bytes (SHA-256 → 32 bytes → bytes32 on-chain)
        byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(pdfBytes);
        String hexHash = "0x" + HexFormat.of().formatHex(hashBytes);
        String digitalSignature = digitalSignatureService.signHash(hashBytes);

        // Check for duplicate document
        if (certificateRecordRepository.findByDocumentHash(hexHash).isPresent()) {
            throw new BadRequestException("A certificate with this document already exists");
        }

        // 2. Upload PDF to Pinata/IPFS
        log.info("Uploading certificate PDF to IPFS for student: {}", req.getStudentId());
        String ipfsCID = pinataService.uploadPdf(pdf);

        // 3. Build certId
        String certId = (req.getCertId() != null && !req.getCertId().isBlank())
                ? req.getCertId()
                : "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (certificateRecordRepository.existsById(certId)) {
            throw new BadRequestException("Verification ID already exists. Please use a different certificate ID.");
        }

        // 4. Send transaction to blockchain
        log.info("Issuing certificate {} on blockchain...", certId);
        TransactionReceipt receipt = registry.issueCertificate(
                certId,
                hashBytes,
                ipfsCID,
                institutionId,
                req.getStudentId(),
                req.getStudentName(),
                req.getCourseName()
        ).send();

        log.info("Certificate issued. TxHash: {}", receipt.getTransactionHash());

        // 5. Save off-chain mirror record
        CertificateRecord record = CertificateRecord.builder()
                .certId(certId)
                .institutionId(institutionId)
                .institutionName(institution.getName())
                .studentId(req.getStudentId())
                .studentName(req.getStudentName())
                .courseName(req.getCourseName())
                .ipfsCID(ipfsCID)
                .documentHash(hexHash)
                .digitalSignature(digitalSignature)
                .signatureAlgorithm(digitalSignatureService.getSignatureAlgorithm())
                .txHash(receipt.getTransactionHash())
                .status(CertStatus.ACTIVE)
                .build();

        certificateRecordRepository.save(record);

        CertificateResponse response = mapToResponse(record);
        sendIssueNotifications(institution, student, student.getFullName(), response);
        return response;
    }

    // ── Revoke Certificate ────────────────────────────────────────────────────

    @Transactional
    public void revokeCertificate(String certId, String institutionId) throws Exception {
        CertificateRecord record = certificateRecordRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certId));

        if (!record.getInstitutionId().equals(institutionId)) {
            throw new BadRequestException("You can only revoke certificates issued by your institution");
        }

        if (record.getStatus() == CertStatus.REVOKED) {
            throw new BadRequestException("Certificate is already revoked");
        }

        // Revoke on blockchain
        registry.revokeCertificate(certId).send();

        // Update local record
        record.setStatus(CertStatus.REVOKED);
        record.setRevokedAt(LocalDateTime.now());
        certificateRecordRepository.save(record);

        log.info("Certificate {} revoked by institution {}", certId, institutionId);
        sendRevokeNotifications(record);
    }

    // ── Verify by Certificate ID ──────────────────────────────────────────────

    public VerifyResponse verifyById(String certId) throws Exception {
        CertificateRegistry.VerifyResult result = registry.verifyCertificateById(certId).send();
        CertificateRecord localRecord = certificateRecordRepository.findById(certId).orElse(null);

        boolean onChainRecordExists =
                (result.institution != null && !result.institution.isBlank())
                        || (result.ipfsCID != null && !result.ipfsCID.isBlank())
                        || (result.issuedAt != null && result.issuedAt.longValue() > 0L);

        boolean signatureValid = false;
        if (localRecord != null && localRecord.getDocumentHash() != null) {
            signatureValid = digitalSignatureService.verifyHash(
                    hexToBytes(localRecord.getDocumentHash()),
                    localRecord.getDigitalSignature()
            );
        }

        String status;
        if (!onChainRecordExists) {
            status = "NOT_FOUND";
        } else {
            status = result.status != null && result.status.intValue() == 0 ? "ACTIVE" : "REVOKED";
        }

        boolean valid = onChainRecordExists && result.isValid && signatureValid;
        String message;
        if (!onChainRecordExists) {
            message = "Certificate was not found on-chain";
        } else if (!signatureValid) {
            message = "Certificate exists on-chain but digital signature validation failed";
        } else if (result.isValid) {
            message = "Certificate is authentic and valid";
        } else {
            message = "Certificate is invalid or has been revoked";
        }

        return VerifyResponse.builder()
                .valid(valid)
                .certId(certId)
                .status(status)
                .institution(result.institution)
                .student(result.student)
                .course(result.course)
                .hashMatched(onChainRecordExists)
                .signatureValid(signatureValid)
                .issuedAt(result.issuedAt != null ? result.issuedAt.longValue() : 0L)
                .ipfsCID(result.ipfsCID)
                .message(message)
                .build();
    }

    // ── Verify by Uploaded PDF ────────────────────────────────────────────────

    public VerifyResponse verifyByDocument(MultipartFile pdf) throws Exception {
        byte[] pdfBytes = validateAndReadPdf(pdf);
        byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(pdfBytes);

        Tuple2<Boolean, String> result = registry.verifyCertificateByHash(hashBytes).send();

        String certId = result.component2();

        if (certId == null || certId.isBlank()) {
            return VerifyResponse.builder()
                    .valid(false)
                    .hashMatched(false)
                    .signatureValid(false)
                    .message("No matching certificate found for this document. It may be altered or unregistered.")
                    .build();
        }

        // Get full details from chain
        VerifyResponse response = verifyById(certId);
        response.setHashMatched(true);
        return response;
    }

    // ── Student: get my certificates ──────────────────────────────────────────

    public List<CertificateResponse> getCertificatesForStudent(String studentId) {
        return certificateRecordRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Institution: get issued certificates ──────────────────────────────────

    public List<CertificateResponse> getCertificatesForInstitution(String institutionId) {
        return certificateRecordRepository.findByInstitutionId(institutionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificateResponse> getAllCertificates() {
        return certificateRecordRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(CertificateRecord::getIssuedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Get single certificate ────────────────────────────────────────────────

    public CertificateResponse getCertificate(String certId) {
        CertificateRecord record = certificateRecordRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certId));
        return mapToResponse(record);
    }

    public byte[] downloadCertificateFile(String certId, User requester) {
        CertificateRecord record = certificateRecordRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certId));

        boolean canDownload = switch (requester.getRole()) {
            case ADMINISTRATOR, ADMIN -> true;
            case STUDENT -> requester.getStudentId() != null
                    && requester.getStudentId().equals(record.getStudentId());
            case INSTITUTION -> requester.getInstitutionId() != null
                    && requester.getInstitutionId().equals(record.getInstitutionId());
        };

        if (!canDownload) {
            throw new ForbiddenException("You do not have permission to download this certificate");
        }

        return pinataService.fetchPdfByCid(record.getIpfsCID());
    }

    public byte[] downloadCertificateFile(String certId) {
        CertificateRecord record = certificateRecordRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certId));
        return pinataService.fetchPdfByCid(record.getIpfsCID());
    }

    public byte[] downloadCertificateFileByCid(String cid) {
        return pinataService.fetchPdfByCid(cid);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private CertificateResponse mapToResponse(CertificateRecord r) {
        return CertificateResponse.builder()
                .certId(r.getCertId())
                .institutionId(r.getInstitutionId())
                .institutionName(r.getInstitutionName())
                .studentId(r.getStudentId())
                .studentName(r.getStudentName())
                .courseName(r.getCourseName())
                .ipfsCID(r.getIpfsCID())
                .ipfsUrl(pinataService.buildGatewayUrl(r.getIpfsCID()))
                .documentHash(r.getDocumentHash())
                .digitalSignature(r.getDigitalSignature())
                .signatureAlgorithm(r.getSignatureAlgorithm())
                .txHash(r.getTxHash())
                .status(r.getStatus().name())
                .issuedAt(r.getIssuedAt())
                .revokedAt(r.getRevokedAt())
                .build();
    }

    private byte[] validateAndReadPdf(MultipartFile pdf) throws Exception {
        if (pdf == null || pdf.isEmpty()) {
            throw new BadRequestException("Certificate PDF is required");
        }
        if (pdf.getSize() > MAX_PDF_SIZE_BYTES) {
            throw new BadRequestException("PDF file is too large. Maximum allowed size is 10MB");
        }
        String contentType = pdf.getContentType();
        if (contentType != null && !"application/pdf".equalsIgnoreCase(contentType)) {
            throw new BadRequestException("Only PDF files are supported");
        }
        return pdf.getBytes();
    }

    private byte[] hexToBytes(String value) {
        String normalized = value.startsWith("0x") ? value.substring(2) : value;
        return HexFormat.of().parseHex(normalized);
    }

    private void sendIssueNotifications(
            Institution institution,
            User student,
            String studentName,
            CertificateResponse certificate
    ) {
        emailNotificationService.sendCertificateIssuedToInstitution(
                institution.getEmail(),
                institution.getName(),
                certificate
        );

        emailNotificationService.sendCertificateIssuedToStudent(
                student.getEmail(),
                studentName,
                certificate
        );
    }

    private void sendRevokeNotifications(CertificateRecord record) {
        CertificateResponse certificate = mapToResponse(record);

        institutionRepository.findById(record.getInstitutionId()).ifPresent(institution ->
                emailNotificationService.sendCertificateRevoked(
                        institution.getEmail(),
                        institution.getName(),
                        certificate
                )
        );

        userRepository.findByStudentId(record.getStudentId()).ifPresent(student ->
                emailNotificationService.sendCertificateRevoked(
                        student.getEmail(),
                        record.getStudentName(),
                        certificate
                )
        );
    }
}
