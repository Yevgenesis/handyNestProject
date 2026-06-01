package com.handynest.auth.session;

public record IssuedRefreshToken(
        RefreshToken refreshToken,
        String rawToken
) {
}
