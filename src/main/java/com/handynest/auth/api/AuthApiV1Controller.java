package com.handynest.auth.api;

import com.handynest.auth.application.AuthV1Service;
import com.handynest.auth.web.RefreshTokenCookieService;
import com.handynest.common.api.ApiConstants;
import com.handynest.common.error.UnauthorizedBusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/auth")
@Tag(name = "Auth", description = "Authentication and session management")
public class AuthApiV1Controller {

    private final AuthV1Service authV1Service;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a personal customer account and opens a session.")
    public ResponseEntity<AuthTokenResponse> register(
            @RequestBody @Valid AuthRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthTokenResponse response = authV1Service.register(request, servletRequest);
        return withRefreshCookie(HttpStatus.CREATED, response).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and opens a session.")
    public ResponseEntity<AuthTokenResponse> login(
            @RequestBody @Valid AuthLoginRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthTokenResponse response = authV1Service.login(request, servletRequest);
        return withRefreshCookie(HttpStatus.OK, response).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh session", description = "Rotates the refresh token and returns a new access token.")
    public ResponseEntity<AuthTokenResponse> refresh(
            @RequestBody(required = false) @Valid RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthTokenResponse response = authV1Service.refresh(
                resolveRefreshToken(request == null ? null : request.refreshToken(), servletRequest),
                servletRequest
        );
        return withRefreshCookie(HttpStatus.OK, response).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the submitted refresh token.")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) @Valid LogoutRequest request,
            HttpServletRequest servletRequest
    ) {
        authV1Service.logout(
                resolveRefreshToken(request == null ? null : request.refreshToken(), servletRequest),
                servletRequest
        );
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieService.clear().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the authenticated user session profile.")
    public AuthUserResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return authV1Service.me(userDetails);
    }

    private ResponseEntity.BodyBuilder withRefreshCookie(HttpStatus status, AuthTokenResponse response) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieService.issue(response.refreshToken()).toString());
    }

    private String resolveRefreshToken(String bodyRefreshToken, HttpServletRequest servletRequest) {
        if (bodyRefreshToken != null && !bodyRefreshToken.isBlank()) {
            return bodyRefreshToken;
        }

        String cookieRefreshToken = refreshTokenCookieService.read(servletRequest);
        if (cookieRefreshToken != null && !cookieRefreshToken.isBlank()) {
            return cookieRefreshToken;
        }

        throw new UnauthorizedBusinessException("Refresh token is required");
    }
}
