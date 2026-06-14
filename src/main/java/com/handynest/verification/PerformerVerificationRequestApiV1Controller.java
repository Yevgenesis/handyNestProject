package com.handynest.verification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performers/me/verification-requests")
@Tag(name = "Verification", description = "Performer verification request lifecycle.")
public class PerformerVerificationRequestApiV1Controller {

  private final VerificationRequestService verificationRequestService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "submitPerformerVerificationRequest",
      summary = "Submit an ID verification request",
      description = "Requires verified phone plus IDENTITY_DOCUMENT and SELFIE documents.")
  public VerificationRequestResponse submit(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody VerificationRequestCreateRequest request) {
    return verificationRequestService.submit(userDetails, request);
  }

  @GetMapping
  @Operation(
      operationId = "listMyPerformerVerificationRequests",
      summary = "List current performer's verification requests")
  public List<VerificationRequestResponse> myRequests(
      @AuthenticationPrincipal UserDetails userDetails) {
    return verificationRequestService.myRequests(userDetails);
  }
}
