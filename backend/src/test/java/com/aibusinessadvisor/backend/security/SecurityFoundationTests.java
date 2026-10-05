package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class SecurityFoundationTests {

    @Autowired
    private AppUserRepository appUserRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private AuthenticationManager
            authenticationManager;


    @Test
    void authenticatesEnabledAnalystWithBcrypt() {

        String rawPassword =
                "StageSecurityTest2026!";

        String passwordHash =
                passwordEncoder.encode(
                        rawPassword
                );


        AppUser user =
                new AppUser(
                        "security.enabled@ai-business-advisor.local",
                        passwordHash,
                        "Security",
                        "Enabled",
                        UserRole.ANALYST,
                        true
                );


        appUserRepository.saveAndFlush(
                user
        );


        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken
                                .unauthenticated(
                                        user.getEmail(),
                                        rawPassword
                                )
                );


        assertThat(
                authentication.isAuthenticated()
        ).isTrue();


        assertThat(
                authentication
                        .getAuthorities()
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
        ).contains(
                "ROLE_ANALYST"
        );


        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        passwordHash
                )
        ).isTrue();


        assertThat(
                passwordHash
        ).isNotEqualTo(
                rawPassword
        );
    }


    @Test
    void rejectsDisabledUser() {

        String rawPassword =
                "DisabledSecurityTest2026!";


        AppUser user =
                new AppUser(
                        "security.disabled@ai-business-advisor.local",
                        passwordEncoder.encode(
                                rawPassword
                        ),
                        "Security",
                        "Disabled",
                        UserRole.ANALYST,
                        false
                );


        appUserRepository.saveAndFlush(
                user
        );


        assertThatThrownBy(
                () ->
                        authenticationManager.authenticate(
                                UsernamePasswordAuthenticationToken
                                        .unauthenticated(
                                                user.getEmail(),
                                                rawPassword
                                        )
                        )
        ).isInstanceOf(
                DisabledException.class
        );
    }
}