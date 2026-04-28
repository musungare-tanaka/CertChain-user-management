package com.austin.msu_cert.blockchain;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRegistry registry;   // injected Web3j wrapper
    private final PinataService pinataService;     // your IPFS uploader

    public IssuanceResult issueCertificate(IssueCertRequest req, MultipartFile pdf) throws Exception {

        // 1. Hash the PDF bytes (must match what the frontend will re-hash for verification)
        byte[] pdfBytes = pdf.getBytes();
        byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(pdfBytes);
        byte[] documentHash = hashBytes; // already 32 bytes — maps to bytes32

        // 2. Upload to Pinata, get CID
        String ipfsCID = pinataService.upload(pdf);

        // 3. Call smart contract — this sends a real on-chain transaction
        TransactionReceipt receipt = registry.issueCertificate(
            req.getCertId(),
            documentHash,
            ipfsCID,
            req.getInstitutionId(),
            req.getStudentId(),
            req.getStudentName(),
            req.getCourseName()
        ).send();  // .send() = write tx (costs gas), .call() = read-only

        return new IssuanceResult(receipt.getTransactionHash(), ipfsCID);
    }

    public VerifyResult verifyCertificate(String certId) throws Exception {
        // .call() = no gas, no tx — just reads state
        CertificateRegistry.VerifyResult result =
            registry.verifyCertificateById(certId).send();
        return map(result);
    }

    public VerifyResult verifyByUpload(MultipartFile pdf) throws Exception {
        byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(pdf.getBytes());
        // Returns (bool isValid, string certId)
        CertificateRegistry.VerifyCertificateByHashResponse response =
            registry.verifyCertificateByHash(hashBytes).send();
        return map(response);
    }
}