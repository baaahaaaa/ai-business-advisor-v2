package com.aibusinessadvisor.backend.admin.user.service;

import com.aibusinessadvisor.backend.admin.user.dto.AdminUserResponse;
import com.aibusinessadvisor.backend.admin.user.dto.CreateAdminUserRequest;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Service
public class AdminUserService {

    private final AppUserRepository
            appUserRepository;

    private final PasswordEncoder
            passwordEncoder;


    public AdminUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.appUserRepository =
                appUserRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {

        return appUserRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    @Transactional
    public AdminUserResponse createUser(
            CreateAdminUserRequest request
    ) {

        String email =
                normalizeEmail(
                        request.email()
                );


        if (appUserRepository
                .existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user with this email already exists."
            );
        }


        AppUser user =
                new AppUser(
                        email,
                        passwordEncoder.encode(
                                request.password()
                        ),
                        request
                                .firstName()
                                .trim(),
                        request
                                .lastName()
                                .trim(),
                        request.role(),
                        true
                );


        AppUser savedUser =
                appUserRepository
                        .saveAndFlush(
                                user
                        );


        return toResponse(
                savedUser
        );
    }


    @Transactional
    public AdminUserResponse setEnabled(
            UUID userId,
            boolean enabled,
            String authenticatedEmail
    ) {

        AppUser target =
                appUserRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found."
                                        )
                        );


        String actorEmail =
                normalizeEmail(
                        authenticatedEmail
                );


        if (!enabled
                && target
                        .getEmail()
                        .equalsIgnoreCase(
                                actorEmail
                        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An administrator cannot disable their own account."
            );
        }


        target.setEnabled(
                enabled
        );


        return toResponse(
                appUserRepository
                        .saveAndFlush(
                                target
                        )
        );
    }


    private AdminUserResponse toResponse(
            AppUser user
    ) {

        return new AdminUserResponse(
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