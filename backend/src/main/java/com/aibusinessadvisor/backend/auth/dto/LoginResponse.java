package com.aibusinessadvisor.backend.auth.dto;


public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresInSeconds,
        CurrentUserResponse user

) {
}