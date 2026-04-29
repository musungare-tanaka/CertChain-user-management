package com.austin.msu_cert.repository;

import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.enums.InstitutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, String id);
    Optional<Institution> findByEmail(String email);
    List<Institution> findByStatus(InstitutionStatus status);
}
