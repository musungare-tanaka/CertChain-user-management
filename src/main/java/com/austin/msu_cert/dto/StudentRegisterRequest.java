package com.austin.msu_cert.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentRegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * The student's official ID number (e.g. R123456X).
     * This becomes the studentId on the blockchain.
     */
    @NotBlank
    private String studentId;
}