package com.austin.msu_cert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionResponse {
    private String id;
    private String name;
    private String registrationNumber;
    private String email;
    private String contactPerson;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}