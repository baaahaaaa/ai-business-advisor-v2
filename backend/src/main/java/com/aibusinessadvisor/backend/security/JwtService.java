package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final long expirationSeconds;

    private final String issuer;


    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration-seconds}")
            long expirationSeconds,
            @Value("${app.jwt.issuer}")
            String issuer
    ) {

        if (expirationSeconds <= 0) {

            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero."
            );
        }


        this.jwtEncoder =
                jwtEncoder;

        this.expirationSeconds =
                expirationSeconds;

        this.issuer =
                issuer;
    }


    public String issueToken(
            AppUser user
    ) {

        if (user.getId() == null) {

            throw new IllegalArgumentException(
                    "Cannot issue a JWT for a user without an id."
            );
        }


        Instant issuedAt =
                Instant.now();

        Instant expiresAt =
                issuedAt.plusSeconds(
                        expirationSeconds
                );


        JwtClaimsSet claims =
                JwtClaimsSet
                        .builder()

                        .issuer(
                                issuer
                        )

                        .subject(
                                user.getEmail()
                        )

                        .issuedAt(
                                issuedAt
                        )

                        .expiresAt(
                                expiresAt
                        )

                        .claim(
                                "uid",
                                user.getId()
                                        .toString()
                        )

                        .claim(
                                "roles",
                                List.of(
                                        user
                                                .getRole()
                                                .name()
                                )
                        )

                        .build();


        return jwtEncoder
                .encode(
                        JwtEncoderParameters
                                .from(
                                        claims
                                )
                )
                .getTokenValue();
    }


    public long getExpirationSeconds() {

        return expirationSeconds;
    }
}