package com.austin.msu_cert.repository;

import com.austin.msu_cert.entity.CertificateRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRecordRepository extends JpaRepository<CertificateRecord, String> {
    List<CertificateRecord> findByStudentId(String studentId);
    List<CertificateRecord> findByInstitutionId(String institutionId);
    boolean existsByInstitutionId(String institutionId);
    Optional<CertificateRecord> findByDocumentHash(String documentHash);
}
