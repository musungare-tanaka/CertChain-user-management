package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.InstitutionResponse;
import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.InstitutionStatus;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.exceptions.ResourceNotFoundException;
import com.austin.msu_cert.repository.InstitutionRepository;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;

    public List<InstitutionResponse> getAllInstitutions() {
        return institutionRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<InstitutionResponse> getByStatus(InstitutionStatus status) {
        return institutionRepository.findByStatus(status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public InstitutionResponse getById(String id) {
        return mapToResponse(findOrThrow(id));
    }

    @Transactional
    public InstitutionResponse approveInstitution(String id) {
        Institution institution = findOrThrow(id);
        if (institution.getStatus() == InstitutionStatus.APPROVED) {
            throw new BadRequestException("Institution is already approved");
        }
        institution.setStatus(InstitutionStatus.APPROVED);
        institution.setApprovedAt(LocalDateTime.now());
        institutionRepository.save(institution);

        // Enable the institution's user account
        userRepository.findAll().stream()
                .filter(u -> id.equals(u.getInstitutionId()))
                .forEach(u -> {
                    u.setEnabled(true);
                    userRepository.save(u);
                });

        log.info("Institution {} approved", id);
        return mapToResponse(institution);
    }

    @Transactional
    public InstitutionResponse suspendInstitution(String id) {
        Institution institution = findOrThrow(id);
        institution.setStatus(InstitutionStatus.SUSPENDED);
        institutionRepository.save(institution);

        // Disable the institution's user account
        userRepository.findAll().stream()
                .filter(u -> id.equals(u.getInstitutionId()))
                .forEach(u -> {
                    u.setEnabled(false);
                    userRepository.save(u);
                });

        log.info("Institution {} suspended", id);
        return mapToResponse(institution);
    }

    private Institution findOrThrow(String id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found: " + id));
    }

    private InstitutionResponse mapToResponse(Institution inst) {
        return InstitutionResponse.builder()
                .id(inst.getId())
                .name(inst.getName())
                .registrationNumber(inst.getRegistrationNumber())
                .email(inst.getEmail())
                .contactPerson(inst.getContactPerson())
                .phone(inst.getPhone())
                .status(inst.getStatus().name())
                .createdAt(inst.getCreatedAt())
                .approvedAt(inst.getApprovedAt())
                .build();
    }
}