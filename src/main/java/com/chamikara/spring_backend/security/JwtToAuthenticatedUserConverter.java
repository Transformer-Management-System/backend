package com.chamikara.spring_backend.security;

import com.chamikara.spring_backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtToAuthenticatedUserConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final LocalUserSyncService localUserSyncService;
    private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        User localUser = localUserSyncService.syncFromJwt(jwt);

        AuthenticatedUser principal = new AuthenticatedUser(
                localUser.getId(),
                localUser.getKeycloakId(),
                localUser.getUsername(),
                localUser.getEmail()
        );

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }
        authorities.addAll(extractRealmRoles(jwt));

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccessClaim = jwt.getClaim("realm_access");
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }

        Object rolesClaim = realmAccess.get("roles");
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return List.of();
        }

        Set<GrantedAuthority> mapped = roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::toRoleAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        if (mapped.isEmpty()) {
            return Collections.emptyList();
        }

        return mapped;
    }

    private String toRoleAuthority(String role) {
        String normalized = role.toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
