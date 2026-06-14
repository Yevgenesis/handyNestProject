package com.handynest.verification;

import com.handynest.marketplace.AttachmentDownloadUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/verification-requests")
@Tag(
    name = "Admin Verification",
    description = "Manual verification review and audited document access.")
public class AdminVerificationRequestApiV1Controller {

  private final VerificationRequestService verificationRequestService;

  @GetMapping
  @Operation(operationId = "listAdminVerificationRequests", summary = "List verification requests")
  public List<VerificationRequestResponse> list(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) VerificationRequestStatus status) {
    return verificationRequestService.adminList(userDetails, status);
  }

  @PostMapping("/{requestId}/approve")
  @Operation(operationId = "approveVerificationRequest", summary = "Approve verification request")
  public VerificationRequestResponse approve(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String requestId,
      @Valid @RequestBody(required = false) VerificationDecisionRequest request) {
    return verificationRequestService.approve(userDetails, requestId, request);
  }

  @PostMapping("/{requestId}/reject")
  @Operation(operationId = "rejectVerificationRequest", summary = "Reject verification request")
  public VerificationRequestResponse reject(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String requestId,
      @Valid @RequestBody(required = false) VerificationDecisionRequest request) {
    return verificationRequestService.reject(userDetails, requestId, request);
  }

  @PostMapping("/{requestId}/documents/{documentId}/download-url")
  @Operation(
      operationId = "createAdminVerificationDocumentDownloadUrl",
      summary = "Create audited verification document download URL",
      description =
          "Admin-only operation. Every issued URL is recorded in the verification audit ledger.")
  public AttachmentDownloadUrlResponse documentDownloadUrl(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String requestId,
      @PathVariable String documentId) {
    return verificationRequestService.adminDocumentDownloadUrl(userDetails, requestId, documentId);
  }
}
