package com.austin.msu_cert.service;

import com.austin.msu_cert.exceptions.BlockchainException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class DigitalSignatureService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_ALGORITHM = "RSA";

    @Value("${app.certificate-signing.private-key-base64:}")
    private String privateKeyBase64;

    @Value("${app.certificate-signing.public-key-base64:}")
    private String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            if (!privateKeyBase64.isBlank() && !publicKeyBase64.isBlank()) {
                KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
                privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                log.info("Loaded certificate signing keys from configuration");
                return;
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            log.warn("Certificate signing keys are not configured. Generated ephemeral development keys.");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize digital signature keys", ex);
        }
    }

    public String getSignatureAlgorithm() {
        return SIGNATURE_ALGORITHM;
    }

    public String signHash(byte[] hashBytes) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(hashBytes);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception ex) {
            throw new BlockchainException("Failed to sign certificate hash", ex);
        }
    }

    public boolean verifyHash(byte[] hashBytes, String encodedSignature) {
        if (encodedSignature == null || encodedSignature.isBlank()) {
            return false;
        }

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(hashBytes);
            byte[] signatureBytes = Base64.getDecoder().decode(encodedSignature);
            return signature.verify(signatureBytes);
        } catch (Exception ex) {
            log.warn("Digital signature verification failed: {}", ex.getMessage());
            return false;
        }
    }
}
