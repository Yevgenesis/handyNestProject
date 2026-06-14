package com.handynest.identity;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1)
@Tag(
    name = "Legal Consents",
    description = "Legal document versions and immutable user acceptances")
public class UserConsentApiV1Controller {

  private final UserConsentService userConsentService;

  @GetMapping("/legal/consent-requirements")
  @Operation(
      operationId = "listConsentRequirements",
      summary = "List current legal consent versions")
  public List<ConsentRequirementResponse> requirements() {
    return userConsentService.requirements();
  }

  @GetMapping("/users/me/consents")
  @Operation(operationId = "listMyConsents", summary = "List current user's accepted consents")
  public List<UserConsentResponse> myConsents(@AuthenticationPrincipal UserDetails userDetails) {
    return userConsentService.myConsents(userDetails);
  }

  @PostMapping("/users/me/consents")
  @Operation(operationId = "acceptUserConsents", summary = "Accept current legal document versions")
  public List<UserConsentResponse> accept(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody @NotEmpty List<@Valid ConsentAcceptanceRequest> requests,
      HttpServletRequest servletRequest) {
    return userConsentService.accept(userDetails, requests, servletRequest);
  }
}
