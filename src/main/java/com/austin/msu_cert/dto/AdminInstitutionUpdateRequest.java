package com.austin.msu_cert.dto;

import com.austin.msu_cert.enums.InstitutionStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminInstitutionUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String registrationNumber;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String contactPerson;

    private String phone;

    @NotNull
    private InstitutionStatus status;
}
