package com.austin.msu_cert.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PinataService {

    private static final String LOCAL_CID_PREFIX = "local-";

    @Value("${pinata.jwt}")
    private String jwt;

    @Value("${pinata.base-url}")
    private String baseUrl;

    @Value("${pinata.gateway-url}")
    private String gatewayUrl;

    @Value("${app.local-storage.path:/tmp/msu-cert-storage}")
    private String localStoragePath;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Uploads a PDF to Pinata/IPFS and returns the CID.
     */
    public String uploadPdf(MultipartFile file) throws IOException {
        if (jwt == null || jwt.isBlank() || jwt.startsWith("ENC(")) {
            return saveToLocalStorage(file);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(jwt);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : "certificate.pdf";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/pinning/pinFileToIPFS",
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String cid = (String) response.getBody().get("IpfsHash");
            log.info("PDF uploaded to IPFS. CID: {}", cid);
            return cid;
        }

        throw new RuntimeException("Pinata upload failed: " + response.getStatusCode());
    }

    /**
     * Returns the public gateway URL for a given CID.
     */
    public String buildGatewayUrl(String cid) {
        if (cid != null && cid.startsWith(LOCAL_CID_PREFIX)) {
            return "/api/certificates/storage/" + cid;
        }
        return gatewayUrl + "/" + cid;
    }

    public byte[] fetchPdfByCid(String cid) {
        try {
            if (cid.startsWith(LOCAL_CID_PREFIX)) {
                Path path = Path.of(localStoragePath, cid + ".pdf");
                return Files.readAllBytes(path);
            }

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    buildGatewayUrl(cid),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch PDF for CID {}: {}", cid, ex.getMessage(), ex);
        }
        throw new RuntimeException("Unable to fetch certificate file for CID: " + cid);
    }

    private String saveToLocalStorage(MultipartFile file) throws IOException {
        Path directory = Path.of(localStoragePath);
        Files.createDirectories(directory);
        String cid = LOCAL_CID_PREFIX + UUID.randomUUID();
        Path outputPath = directory.resolve(cid + ".pdf");
        Files.write(outputPath, file.getBytes(), StandardOpenOption.CREATE_NEW);
        log.info("Stored certificate PDF in local off-chain storage. CID: {}", cid);
        return cid;
    }
}
