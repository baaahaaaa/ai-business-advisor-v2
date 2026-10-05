package com.aibusinessadvisor.backend.security;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository appUserRepository;


    public DatabaseUserDetailsService(
            AppUserRepository appUserRepository
    ) {

        this.appUserRepository =
                appUserRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        String normalizedEmail =
                normalizeEmail(username);


        AppUser user =
                appUserRepository
                        .findByEmail(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Invalid credentials."
                                        )
                        );


        return User
                .withUsername(
                        user.getEmail()
                )
                .password(
                        user.getPasswordHash()
                )
                .roles(
                        user.getRole().name()
                )
                .disabled(
                        !user.isEnabled()
                )
                .build();
    }


    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}