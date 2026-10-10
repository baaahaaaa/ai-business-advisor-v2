package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAccountStatusValidatorTest {

    private static final String EMAIL =
            "analyst@example.test";

    private static final UUID USER_ID =
            UUID.fromString(
                    "11111111-1111-4111-8111-111111111111"
            );

    private AppUserRepository repository;
    private JwtAccountStatusValidator validator;

    @BeforeEach
    void setUp() {
        repository = mock(AppUserRepository.class);
        validator = new JwtAccountStatusValidator(repository);
    }

    private void mockAccount(boolean enabled, UserRole role) {
        AppUser user = mock(AppUser.class);

        when(user.isEnabled()).thenReturn(enabled);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getRole()).thenReturn(role);

        when(repository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));
    }

    private Jwt jwt(
            String email,
            String uid,
            List<String> roles
    ) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(email);

        if (uid != null) {
            builder.claim("uid", uid);
        }

        if (roles != null) {
            builder.claim("roles", roles);
        }

        return builder.build();
    }

    private Jwt analystToken() {
        return jwt(
                EMAIL,
                USER_ID.toString(),
                List.of("ANALYST")
        );
    }

    @Test
    void acceptsActiveAccountWithMatchingClaims() {
        mockAccount(true, UserRole.ANALYST);

        assertFalse(
                validator.validate(analystToken()).hasErrors()
        );
    }

    @Test
    void rejectsDisabledAccount() {
        mockAccount(false, UserRole.ANALYST);

        assertTrue(
                validator.validate(analystToken()).hasErrors()
        );
    }

    @Test
    void rejectsDeletedOrUnknownAccount() {
        when(repository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertTrue(
                validator.validate(analystToken()).hasErrors()
        );
    }

    @Test
    void rejectsMismatchedUserId() {
        mockAccount(true, UserRole.ANALYST);

        Jwt token = jwt(
                EMAIL,
                "22222222-2222-4222-8222-222222222222",
                List.of("ANALYST")
        );

        assertTrue(
                validator.validate(token).hasErrors()
        );
    }

    @Test
    void rejectsChangedAccountRole() {
        mockAccount(true, UserRole.ADMIN);

        assertTrue(
                validator.validate(analystToken()).hasErrors()
        );
    }

    @Test
    void rejectsMissingUserIdClaim() {
        Jwt token = jwt(
                EMAIL,
                null,
                List.of("ANALYST")
        );

        assertTrue(
                validator.validate(token).hasErrors()
        );
    }

    @Test
    void rejectsMissingRolesClaim() {
        Jwt token = jwt(
                EMAIL,
                USER_ID.toString(),
                null
        );

        assertTrue(
                validator.validate(token).hasErrors()
        );
    }

    @Test
    void rejectsMultipleRoles() {
        Jwt token = jwt(
                EMAIL,
                USER_ID.toString(),
                List.of("ANALYST", "ADMIN")
        );

        assertTrue(
                validator.validate(token).hasErrors()
        );
    }

    @Test
    void normalizesEmailBeforeDatabaseLookup() {
        mockAccount(true, UserRole.ANALYST);

        Jwt token = jwt(
                "  ANALYST@EXAMPLE.TEST  ",
                USER_ID.toString(),
                List.of("ANALYST")
        );

        assertFalse(
                validator.validate(token).hasErrors()
        );

        verify(repository).findByEmail(EMAIL);
    }

    @Test
    void rejectsBlankSubject() {
        Jwt token = jwt(
                "",
                USER_ID.toString(),
                List.of("ANALYST")
        );

        assertTrue(
                validator.validate(token).hasErrors()
        );
    }
}
