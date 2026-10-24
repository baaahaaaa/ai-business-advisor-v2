package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class JwtFoundationTests {

    @Autowired
    private AppUserRepository appUserRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtService jwtService;


    @Autowired
    private JwtDecoder jwtDecoder;


    @Test
    void issuesAndValidatesAnalystToken() {

        AppUser user =
                new AppUser(
                        "jwt.test@ai-business-advisor.local",
                        passwordEncoder.encode(
                                "JwtFoundationTest2026!"
                        ),
                        "Jwt",
                        "Test",
                        UserRole.ANALYST,
                        true
                );


        AppUser savedUser =
                appUserRepository
                        .saveAndFlush(
                                user
                        );


        String token =
                jwtService.issueToken(
                        savedUser
                );


        Jwt decoded =
                jwtDecoder.decode(
                        token
                );


        assertThat(
                token
        ).isNotBlank();


        assertThat(
                decoded.getSubject()
        ).isEqualTo(
                savedUser.getEmail()
        );


        assertThat(
                decoded.getClaimAsString(
                        "uid"
                )
        ).isEqualTo(
                savedUser
                        .getId()
                        .toString()
        );


        assertThat(
                decoded.getClaimAsStringList(
                        "roles"
                )
        ).containsExactly(
                UserRole.ANALYST.name()
        );


        assertThat(
                decoded.getIssuedAt()
        ).isNotNull();


        assertThat(
                decoded.getExpiresAt()
        ).isNotNull();


        assertThat(
                decoded.getExpiresAt()
        ).isAfter(
                decoded.getIssuedAt()
        );


        assertThat(
                jwtService.getExpirationSeconds()
        ).isEqualTo(
                3600L
        );
    }
}