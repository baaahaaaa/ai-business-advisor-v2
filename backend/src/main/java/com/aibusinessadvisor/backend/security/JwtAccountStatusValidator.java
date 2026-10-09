package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class JwtAccountStatusValidator
        implements OAuth2TokenValidator<Jwt> {

    private final AppUserRepository userRepository;

    public JwtAccountStatusValidator(
            AppUserRepository userRepository
    ) {
        this.userRepository =
                Objects.requireNonNull(userRepository);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String email = jwt.getSubject();
        String userId = jwt.getClaimAsString("uid");
        List<String> roles = jwt.getClaimAsStringList("roles");

        if (email == null
                || email.isBlank()
                || userId == null
                || roles == null
                || roles.size() != 1) {

            return invalidToken();
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        boolean validAccount = userRepository
                .findByEmail(normalizedEmail)
                .filter(AppUser::isEnabled)
                .filter(user ->
                        user.getId() != null
                                && user.getId().toString().equals(userId)
                )
                .filter(user ->
                        roles.get(0).equals(user.getRole().name())
                )
                .isPresent();

        return validAccount
                ? OAuth2TokenValidatorResult.success()
                : invalidToken();
    }

    private OAuth2TokenValidatorResult invalidToken() {

        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(
                        "invalid_token",
                        "Token is no longer valid for this account.",
                        null
                )
        );
    }
}
