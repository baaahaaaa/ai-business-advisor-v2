package com.aibusinessadvisor.backend.admin;

import com.aibusinessadvisor.backend.admin.user.dto.CreateAdminUserRequest;
import com.aibusinessadvisor.backend.admin.user.service.AdminUserService;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class AdminUserServiceTests {

    @Autowired
    private AdminUserService adminUserService;


    @Autowired
    private AppUserRepository appUserRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void createsAnalystWithBcryptPassword() {

        var response =
                adminUserService
                        .createUser(
                                new CreateAdminUserRequest(
                                        "new.analyst@ai-business-advisor.local",
                                        "AnalystTest2026!",
                                        "New",
                                        "Analyst",
                                        UserRole.ANALYST
                                )
                        );


        AppUser saved =
                appUserRepository
                        .findByEmail(
                                response.email()
                        )
                        .orElseThrow();


        assertThat(
                response.role()
        ).isEqualTo(
                UserRole.ANALYST
        );


        assertThat(
                response.enabled()
        ).isTrue();


        assertThat(
                saved.getPasswordHash()
        ).isNotEqualTo(
                "AnalystTest2026!"
        );


        assertThat(
                passwordEncoder.matches(
                        "AnalystTest2026!",
                        saved.getPasswordHash()
                )
        ).isTrue();
    }


    @Test
    void rejectsDuplicateEmail() {

        adminUserService
                .createUser(
                        new CreateAdminUserRequest(
                                "duplicate@ai-business-advisor.local",
                                "DuplicateTest2026!",
                                "Duplicate",
                                "One",
                                UserRole.ANALYST
                        )
                );


        assertThatThrownBy(
                () ->
                        adminUserService
                                .createUser(
                                        new CreateAdminUserRequest(
                                                "DUPLICATE@ai-business-advisor.local",
                                                "DuplicateTest2026!",
                                                "Duplicate",
                                                "Two",
                                                UserRole.ANALYST
                                        )
                                )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "409"
                );
    }


    @Test
    void preventsAdminFromDisablingOwnAccount() {

        AppUser admin =
                new AppUser(
                        "self.admin@ai-business-advisor.local",
                        passwordEncoder.encode(
                                "SelfAdminTest2026!"
                        ),
                        "Self",
                        "Admin",
                        UserRole.ADMIN,
                        true
                );


        AppUser savedAdmin =
                appUserRepository
                        .saveAndFlush(
                                admin
                        );


        assertThatThrownBy(
                () ->
                        adminUserService
                                .setEnabled(
                                        savedAdmin.getId(),
                                        false,
                                        savedAdmin.getEmail()
                                )
        )
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .hasMessageContaining(
                        "400"
                );
    }
}