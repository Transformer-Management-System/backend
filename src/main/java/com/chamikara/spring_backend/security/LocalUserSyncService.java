package com.chamikara.spring_backend.security;

import com.chamikara.spring_backend.entity.User;
import com.chamikara.spring_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalUserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User syncFromJwt(Jwt jwt) {
        UUID keycloakId = extractKeycloakId(jwt);
        String username = defaultIfBlank(jwt.getClaimAsString("preferred_username"), keycloakId.toString());
        String email = defaultIfBlank(jwt.getClaimAsString("email"), username + "@local.invalid");

        return userRepository.findByKeycloakId(keycloakId)
                .map(existing -> updateIfChanged(existing, username, email))
                .orElseGet(() -> createUser(keycloakId, username, email));
    }

    private User updateIfChanged(User existing, String username, String email) {
        boolean changed = false;

        if (!Objects.equals(existing.getUsername(), username)) {
            existing.setUsername(username);
            changed = true;
        }

        if (!Objects.equals(existing.getEmail(), email)) {
            existing.setEmail(email);
            changed = true;
        }

        if (!changed) {
            return existing;
        }

        return userRepository.save(existing);
    }

    private User createUser(UUID keycloakId, String username, String email) {
        User candidate = User.builder()
                .keycloakId(keycloakId)
                .username(username)
                .email(email)
                .build();

        try {
            User created = userRepository.save(candidate);
            log.info("Provisioned local user for Keycloak subject: {}", keycloakId);
            return created;
        } catch (DataIntegrityViolationException ex) {
            return userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> ex);
        }
    }

    private UUID extractKeycloakId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("JWT is missing subject claim");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw new BadCredentialsException("JWT subject is not a valid UUID", ex);
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
