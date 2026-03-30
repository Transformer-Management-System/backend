package com.chamikara.spring_backend.security;

import java.util.UUID;

public record AuthenticatedUser(
        Long localUserId,
        UUID keycloakId,
        String username,
        String email
) {
}
