package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtDecoderIntegrationTest {

    private static final String ISSUER = "ai-business-advisor-test";
    private static final String EMAIL = "analyst@example.test";

    private static final UUID USER_ID =
            UUID.fromString(
                    "11111111-1111-4111-8111-111111111111"
            );

    private static final SecretKey SECRET =
            new SecretKeySpec(
                    "0123456789abcdef0123456789abcdef"
                            .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

    private static final SecretKey WRONG_SECRET =
            new SecretKeySpec(
                    "abcdef0123456789abcdef0123456789"
                            .getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

    private AppUserRepository repository;
    private AppUser user;
    private NimbusJwtDecoder decoder;

    @BeforeEach
    void setUp() {
        repository = mock(AppUserRepository.class);
        user = mock(AppUser.class);

        when(repository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(USER_ID);
        when(user.isEnabled()).thenReturn(true);
        when(user.getRole()).thenReturn(UserRole.ANALYST);

        decoder = NimbusJwtDecoder.withSecretKey(SECRET)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<Jwt>(
                        JwtValidators.createDefaultWithIssuer(ISSUER),
                        new JwtAccountStatusValidator(repository)
                )
        );
    }

    private String createToken(
            String issuer,
            Instant issuedAt,
            Instant expiresAt,
            SecretKey signingKey
    ) {
        JwtEncoder encoder = NimbusJwtEncoder
                .withSecretKey(signingKey)
                .algorithm(MacAlgorithm.HS256)
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(EMAIL)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("uid", USER_ID.toString())
                .claim("roles", List.of("ANALYST"))
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    private String validToken() {
        Instant now = Instant.now();

        return createToken(
                ISSUER,
                now.minusSeconds(5),
                now.plusSeconds(600),
                SECRET
        );
    }

    @Test
    void acceptsValidJwtForActiveAccount() {
        String token = validToken();

        assertDoesNotThrow(() -> decoder.decode(token));
    }

    @Test
    void rejectsPreviouslyValidJwtAfterDisable() {
        String token = validToken();

        assertDoesNotThrow(() -> decoder.decode(token));

        when(user.isEnabled()).thenReturn(false);

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void rejectsJwtAfterAccountRoleChanges() {
        String token = validToken();

        assertDoesNotThrow(() -> decoder.decode(token));

        when(user.getRole()).thenReturn(UserRole.ADMIN);

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void rejectsJwtWhenAccountNoLongerExists() {
        String token = validToken();

        when(repository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void rejectsExpiredJwt() {
        Instant now = Instant.now();

        String token = createToken(
                ISSUER,
                now.minusSeconds(3600),
                now.minusSeconds(300),
                SECRET
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void rejectsJwtWithInvalidIssuer() {
        Instant now = Instant.now();

        String token = createToken(
                "unknown-issuer",
                now.minusSeconds(5),
                now.plusSeconds(600),
                SECRET
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void rejectsJwtSignedWithWrongSecret() {
        Instant now = Instant.now();

        String token = createToken(
                ISSUER,
                now.minusSeconds(5),
                now.plusSeconds(600),
                WRONG_SECRET
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }
}
