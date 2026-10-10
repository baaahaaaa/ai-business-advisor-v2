package com.aibusinessadvisor.backend.auth.dto;

import com.aibusinessadvisor.backend.user.model.UserRole;

import java.time.Instant;
import java.util.UUID;


public record CurrentUserResponse(

        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt

) {
}