package com.aibusinessadvisor.backend.admin.user.controller;

import com.aibusinessadvisor.backend.admin.user.dto.AdminUserResponse;
import com.aibusinessadvisor.backend.admin.user.dto.CreateAdminUserRequest;
import com.aibusinessadvisor.backend.admin.user.dto.UpdateUserEnabledRequest;

import com.aibusinessadvisor.backend.admin.user.service.AdminUserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService
            adminUserService;


    public AdminUserController(
            AdminUserService adminUserService
    ) {

        this.adminUserService =
                adminUserService;
    }


    @GetMapping
    public List<AdminUserResponse> users() {

        return adminUserService
                .listUsers();
    }


    @PostMapping
    public ResponseEntity<AdminUserResponse>
            createUser(

                    @Valid
                    @RequestBody
                    CreateAdminUserRequest request

            ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        adminUserService
                                .createUser(
                                        request
                                )
                );
    }


    @PatchMapping("/{userId}/enabled")
    public AdminUserResponse setEnabled(

            @PathVariable
            UUID userId,

            @Valid
            @RequestBody
            UpdateUserEnabledRequest request,

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        return adminUserService
                .setEnabled(
                        userId,
                        request.enabled(),
                        jwt.getSubject()
                );
    }
}