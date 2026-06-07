package com.handynest.identity;

import com.handynest.common.api.ApiConstants;
import com.handynest.auth.web.RefreshTokenCookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/users/me")
@Tag(name = "Users", description = "Current user profile")
public class UserProfileApiV1Controller {

    private final UserProfileService userProfileService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public UserProfileResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return userProfileService.me(userDetails);
    }

    @PatchMapping
    @Operation(summary = "Update current user profile")
    public UserProfileResponse updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return userProfileService.updateMe(userDetails, request);
    }

    @DeleteMapping
    @Operation(summary = "Request current account deletion")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        userProfileService.deleteMe(userDetails, servletRequest);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieService.clear().toString())
                .build();
    }
}
