package com.austin.msu_cert.entity;

import com.austin.msu_cert.enums.InstitutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "institutions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    /**
     * UUID — also stored on blockchain as institutionId for every cert issued.
     */
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    /** Official registration/accreditation number */
    @Column(unique = true)
    private String registrationNumber;

    @Column(unique = true, nullable = false)
    private String email;

    private String contactPerson;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InstitutionStatus status = InstitutionStatus.PENDING;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}