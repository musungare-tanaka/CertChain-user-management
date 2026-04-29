package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.AdminInstitutionCreateRequest;
import com.austin.msu_cert.dto.AdminInstitutionUpdateRequest;
import com.austin.msu_cert.dto.InstitutionResponse;
import com.austin.msu_cert.entity.Institution;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.enums.InstitutionStatus;
import com.austin.msu_cert.exceptions.BadRequestException;
import com.austin.msu_cert.exceptions.ResourceNotFoundException;
import com.austin.msu_cert.repository.CertificateRecordRepository;
import com.austin.msu_cert.repository.InstitutionRepository;
import com.austin.msu_cert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final CertificateRecordRepository certificateRecordRepository;
    private final PasswordEncoder passwordEncoder;

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
    public InstitutionResponse createInstitutionByAdmin(AdminInstitutionCreateRequest req) {
        validateCreateRequest(req);

        String institutionId = UUID.randomUUID().toString();
        Institution institution = Institution.builder()
                .id(institutionId)
                .name(req.getName())
                .registrationNumber(req.getRegistrationNumber())
                .email(req.getEmail())
                .contactPerson(req.getContactPerson())
                .phone(req.getPhone())
                .status(InstitutionStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();
        institutionRepository.save(institution);

        User institutionUser = User.builder()
                .fullName(req.getName())
                .email(req.getEmail())
                .username(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.INSTITUTION)
                .institutionId(institutionId)
                .enabled(true)
                .build();
        userRepository.save(institutionUser);

        log.info("Institution {} created by administrator", institutionId);
        return mapToResponse(institution);
    }

    @Transactional
    public InstitutionResponse updateInstitutionByAdmin(String id, AdminInstitutionUpdateRequest req) {
        Institution institution = findOrThrow(id);

        if (institutionRepository.existsByNameAndIdNot(req.getName(), id)) {
            throw new BadRequestException("Institution name already in use");
        }
        if (institutionRepository.existsByRegistrationNumberAndIdNot(req.getRegistrationNumber(), id)) {
            throw new BadRequestException("Registration number already in use");
        }
        if (institutionRepository.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new BadRequestException("Institution email already in use");
        }
        if (!institution.getEmail().equalsIgnoreCase(req.getEmail())) {
            userRepository.findByEmail(req.getEmail())
                    .filter(existing -> !id.equals(existing.getInstitutionId()))
                    .ifPresent(existing -> {
                        throw new BadRequestException("Email already in use");
                    });
        }

        institution.setName(req.getName());
        institution.setRegistrationNumber(req.getRegistrationNumber());
        institution.setEmail(req.getEmail());
        institution.setContactPerson(req.getContactPerson());
        institution.setPhone(req.getPhone());
        applyInstitutionStatus(institution, req.getStatus());
        institutionRepository.save(institution);

        boolean enableInstitutionUsers = institution.getStatus() == InstitutionStatus.APPROVED;
        List<User> institutionUsers = userRepository.findByInstitutionId(id);
        boolean emailAssigned = false;
        for (User user : institutionUsers) {
            user.setFullName(req.getName());
            if (!emailAssigned) {
                user.setEmail(req.getEmail());
                user.setUsername(req.getEmail());
                emailAssigned = true;
            }
            user.setEnabled(enableInstitutionUsers);
        }
        userRepository.saveAll(institutionUsers);

        log.info("Institution {} updated by administrator", id);
        return mapToResponse(institution);
    }

    @Transactional
    public InstitutionResponse approveInstitution(String id) {
        Institution institution = findOrThrow(id);
        if (institution.getStatus() == InstitutionStatus.APPROVED) {
            throw new BadRequestException("Institution is already approved");
        }

        applyInstitutionStatus(institution, InstitutionStatus.APPROVED);
        institutionRepository.save(institution);
        setInstitutionUsersEnabled(id, true);

        log.info("Institution {} approved", id);
        return mapToResponse(institution);
    }

    @Transactional
    public InstitutionResponse suspendInstitution(String id) {
        Institution institution = findOrThrow(id);
        applyInstitutionStatus(institution, InstitutionStatus.SUSPENDED);
        institutionRepository.save(institution);
        setInstitutionUsersEnabled(id, false);

        log.info("Institution {} suspended", id);
        return mapToResponse(institution);
    }

    @Transactional
    public InstitutionResponse disableInstitution(String id) {
        return suspendInstitution(id);
    }

    @Transactional
    public void deleteInstitutionByAdmin(String id) {
        Institution institution = findOrThrow(id);
        if (certificateRecordRepository.existsByInstitutionId(id)) {
            throw new BadRequestException(
                    "Cannot delete institution with issued certificates. Disable it instead."
            );
        }

        List<User> institutionUsers = userRepository.findByInstitutionId(id);
        userRepository.deleteAll(institutionUsers);
        institutionRepository.delete(institution);

        log.info("Institution {} deleted by administrator", id);
    }

    private void validateCreateRequest(AdminInstitutionCreateRequest req) {
        if (institutionRepository.existsByName(req.getName())) {
            throw new BadRequestException("Institution name already in use");
        }
        if (institutionRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Institution email already in use");
        }
        if (institutionRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
            throw new BadRequestException("Registration number already in use");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
    }

    private void applyInstitutionStatus(Institution institution, InstitutionStatus status) {
        institution.setStatus(status);
        if (status == InstitutionStatus.APPROVED) {
            if (institution.getApprovedAt() == null) {
                institution.setApprovedAt(LocalDateTime.now());
            }
        } else {
            institution.setApprovedAt(null);
        }
    }

    private void setInstitutionUsersEnabled(String institutionId, boolean enabled) {
        List<User> institutionUsers = userRepository.findByInstitutionId(institutionId);
        institutionUsers.forEach(user -> user.setEnabled(enabled));
        userRepository.saveAll(institutionUsers);
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
