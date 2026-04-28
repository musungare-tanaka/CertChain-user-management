package com.austin.msu_cert.repository;

import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.enums.InstitutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
    boolean existsByEmail(String email);
    boolean existsByRegistrationNumber(String registrationNumber);
    Optional<Institution> findByEmail(String email);
    List<Institution> findByStatus(InstitutionStatus status);
}