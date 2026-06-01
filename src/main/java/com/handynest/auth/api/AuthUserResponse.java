package com.handynest.auth.api;

import java.util.Set;

public record AuthUserResponse(
        String publicId,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified,
        Set<String> roles
) {
    public AuthUserResponse {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
