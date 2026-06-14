package com.handynest.moderation;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Complaints", description = "User complaint submission and lifecycle")
public class ComplaintApiV1Controller {

  private final ModerationService moderationService;

  @PostMapping("/complaints")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(operationId = "createComplaint", summary = "Create a complaint")
  public ComplaintResponse create(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Valid @RequestBody ComplaintCreateRequest request) {
    return moderationService.createComplaint(userDetails, idempotencyKey, request);
  }

  @GetMapping("/my/complaints")
  @Operation(operationId = "listMyComplaints", summary = "List my complaints")
  public PageResponse<ComplaintResponse> myComplaints(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return moderationService.myComplaints(userDetails, page, size);
  }

  @PostMapping("/complaints/{complaintId}/cancel")
  @Operation(operationId = "cancelComplaint", summary = "Cancel my open complaint")
  public ComplaintResponse cancel(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable String complaintId) {
    return moderationService.cancelComplaint(userDetails, complaintId);
  }
}
