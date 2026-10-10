package com.aibusinessadvisor.backend.auth.service;

import com.aibusinessadvisor.backend.auth.dto.CurrentUserResponse;
import com.aibusinessadvisor.backend.auth.dto.LoginRequest;
import com.aibusinessadvisor.backend.auth.dto.LoginResponse;

import com.aibusinessadvisor.backend.security.JwtService;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;


@Service
public class AuthService {

    private final AuthenticationManager
            authenticationManager;

    private final AppUserRepository
            appUserRepository;

    private final JwtService
            jwtService;


    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            JwtService jwtService
    ) {

        this.authenticationManager =
                authenticationManager;

        this.appUserRepository =
                appUserRepository;

        this.jwtService =
                jwtService;
    }


    @Transactional
    public LoginResponse login(
            LoginRequest request
    ) {

        String email =
                normalizeEmail(
                        request.email()
                );


        try {

            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(
                                    email,
                                    request.password()
                            )
            );

        }
        catch (AuthenticationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials."
            );
        }


        AppUser user =
                appUserRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid credentials."
                                        )
                        );


        user.setLastLoginAt(
                Instant.now()
        );


        appUserRepository.save(
                user
        );


        String token =
                jwtService.issueToken(
                        user
                );


        return new LoginResponse(
                token,
                "Bearer",
                jwtService
                        .getExpirationSeconds(),
                toResponse(user)
        );
    }


    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(
            String email
    ) {

        AppUser user =
                appUserRepository
                        .findByEmail(
                                normalizeEmail(email)
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Authenticated user not found."
                                        )
                        );


        if (!user.isEnabled()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User account is disabled."
            );
        }


        return toResponse(
                user
        );
    }


    private CurrentUserResponse toResponse(
            AppUser user
    ) {

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }


    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}