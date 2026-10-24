package com.aibusinessadvisor.backend.user.bootstrap;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


@Component
public class AdminBootstrap
        implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AdminBootstrap.class
            );


    private final AppUserRepository
            appUserRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final String adminEmail;

    private final String adminPassword;

    private final String adminFirstName;

    private final String adminLastName;


    public AdminBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,

            @Value("${app.admin.email:}")
            String adminEmail,

            @Value("${app.admin.password:}")
            String adminPassword,

            @Value("${app.admin.first-name:Admin}")
            String adminFirstName,

            @Value("${app.admin.last-name:User}")
            String adminLastName
    ) {

        this.appUserRepository =
                appUserRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.adminEmail =
                adminEmail;

        this.adminPassword =
                adminPassword;

        this.adminFirstName =
                adminFirstName;

        this.adminLastName =
                adminLastName;
    }


    @Override
    @Transactional
    public void run(
            ApplicationArguments arguments
    ) {

        boolean emailConfigured =
                adminEmail != null
                        && !adminEmail.isBlank();

        boolean passwordConfigured =
                adminPassword != null
                        && !adminPassword.isBlank();


        if (!emailConfigured
                && !passwordConfigured) {

            LOGGER.info(
                    "Admin bootstrap is not configured."
            );

            return;
        }


        if (!emailConfigured
                || !passwordConfigured) {

            throw new IllegalStateException(
                    "Both APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD must be configured together."
            );
        }


        if (adminPassword.length() < 12) {

            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD must contain at least 12 characters."
            );
        }


        String normalizedEmail =
                adminEmail
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        var existingUser =
                appUserRepository
                        .findByEmail(
                                normalizedEmail
                        );


        if (existingUser.isPresent()) {

            if (existingUser
                    .get()
                    .getRole()
                    != UserRole.ADMIN) {

                throw new IllegalStateException(
                        "Configured admin email already belongs to a non-admin user."
                );
            }


            LOGGER.info(
                    "Admin user already exists."
            );

            return;
        }


        AppUser admin =
                new AppUser(
                        normalizedEmail,
                        passwordEncoder.encode(
                                adminPassword
                        ),
                        adminFirstName.trim(),
                        adminLastName.trim(),
                        UserRole.ADMIN,
                        true
                );


        appUserRepository.save(
                admin
        );


        LOGGER.info(
                "Initial admin user created."
        );
    }
}