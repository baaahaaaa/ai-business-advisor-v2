package com.aibusinessadvisor.backend.admin.user.dto;

import com.aibusinessadvisor.backend.user.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateAdminUserRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 12, max = 100)
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        UserRole role

) {
}