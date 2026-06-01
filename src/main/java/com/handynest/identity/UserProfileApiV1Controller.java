package com.handynest.identity;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
}
