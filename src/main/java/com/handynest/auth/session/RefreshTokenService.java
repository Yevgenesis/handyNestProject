package com.handynest.auth.session;

import codezilla.handynestproject.model.entity.User;
import com.handynest.common.error.UnauthorizedBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 64;
    private static final int IP_MAX_LENGTH = 64;
    private static final int USER_AGENT_MAX_LENGTH = 512;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.jwt.refresh-token-ttl:30d}")
    private Duration refreshTokenTtl;

    public IssuedRefreshToken issue(User user, HttpServletRequest servletRequest) {
        String rawToken = newRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(refreshToken.getCreatedAt().plus(refreshTokenTtl));
        refreshToken.setCreatedByIp(truncate(clientIp(servletRequest), IP_MAX_LENGTH));
        refreshToken.setUserAgent(truncate(servletRequest.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH));

        return new IssuedRefreshToken(refreshTokenRepository.save(refreshToken), rawToken);
    }

    public IssuedRefreshToken rotate(String rawToken, HttpServletRequest servletRequest) {
        RefreshToken currentRefreshToken = findActive(rawToken);
        IssuedRefreshToken replacement = issue(currentRefreshToken.getUser(), servletRequest);
        currentRefreshToken.revoke(
                Instant.now(),
                truncate(clientIp(servletRequest), IP_MAX_LENGTH),
                replacement.refreshToken()
        );
        refreshTokenRepository.save(currentRefreshToken);
        return replacement;
    }

    public void revoke(String rawToken, HttpServletRequest servletRequest) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(refreshToken -> refreshToken.getRevokedAt() == null)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke(
                            Instant.now(),
                            truncate(clientIp(servletRequest), IP_MAX_LENGTH),
                            null
                    );
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private RefreshToken findActive(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedBusinessException("Invalid refresh token"));

        if (!refreshToken.isActive(Instant.now())) {
            throw new UnauthorizedBusinessException("Invalid refresh token");
        }

        return refreshToken;
    }

    private String newRawToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is not available", exception);
        }
    }

    private String clientIp(HttpServletRequest servletRequest) {
        String forwardedFor = servletRequest.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
