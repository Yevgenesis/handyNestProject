package com.handynest.auth.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenCookieService {

  public static final String DEFAULT_COOKIE_NAME = "handynest_refresh_token";

  private final String cookieName;
  private final String cookiePath;
  private final String sameSite;
  private final boolean secure;
  private final Duration refreshTokenTtl;

  public RefreshTokenCookieService(
      @Value("${app.security.refresh-cookie.name:" + DEFAULT_COOKIE_NAME + "}") String cookieName,
      @Value("${app.security.refresh-cookie.path:/api/v1/auth}") String cookiePath,
      @Value("${app.security.refresh-cookie.same-site:Lax}") String sameSite,
      @Value("${app.security.refresh-cookie.secure:false}") boolean secure,
      @Value("${app.security.jwt.refresh-token-ttl:30d}") Duration refreshTokenTtl) {
    this.cookieName = cookieName;
    this.cookiePath = cookiePath;
    this.sameSite = sameSite;
    this.secure = secure;
    this.refreshTokenTtl = refreshTokenTtl;
  }

  public ResponseCookie issue(String refreshToken) {
    return base(refreshToken).maxAge(refreshTokenTtl).build();
  }

  public ResponseCookie clear() {
    return base("").maxAge(Duration.ZERO).build();
  }

  public String read(HttpServletRequest servletRequest) {
    Cookie[] cookies = servletRequest.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private ResponseCookie.ResponseCookieBuilder base(String value) {
    return ResponseCookie.from(cookieName, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path(cookiePath);
  }
}
