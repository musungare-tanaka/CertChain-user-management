package com.austin.msu_cert.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionRegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String registrationNumber;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String contactPerson;

    private String phone;
}