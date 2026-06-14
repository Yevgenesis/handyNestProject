package com.handynest.verification;

import com.handynest.marketplace.AttachmentResponse;
import com.handynest.marketplace.AttachmentUploadResponse;
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
@RequestMapping("/api/v1/verification/documents")
@Tag(name = "Verification", description = "Private performer verification documents.")
public class VerificationDocumentApiV1Controller {

  private final VerificationDocumentService verificationDocumentService;

  @PostMapping("/upload-url")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "createVerificationDocumentUploadUrl",
      summary = "Create verification document upload URL",
      description =
          "Creates ADMIN_ONLY metadata and a short-lived upload URL in the verification bucket.")
  public AttachmentUploadResponse createUploadUrl(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody VerificationDocumentUploadRequest request) {
    return verificationDocumentService.createUploadUrl(userDetails, request);
  }

  @GetMapping
  @Operation(
      operationId = "listMyVerificationDocuments",
      summary = "List current user's verification documents")
  public List<AttachmentResponse> myDocuments(@AuthenticationPrincipal UserDetails userDetails) {
    return verificationDocumentService.myDocuments(userDetails);
  }
}
