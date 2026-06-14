package com.handynest.auth.api;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    AuthUserResponse user) {}
