package com.handynest.risk;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminRiskEventApiV1Controller {

  private final RiskEventService riskEventService;

  @GetMapping("/api/v1/admin/risk-events")
  public List<RiskEventResponse> list(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) RiskEventStatus status,
      @RequestParam(required = false) RiskType riskType,
      @RequestParam(required = false) RiskSeverity severity) {
    return riskEventService.adminEvents(userDetails, status, riskType, severity);
  }

  @PostMapping("/api/v1/admin/risk-events/{eventId}/resolve")
  public RiskEventResponse resolve(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String eventId,
      @Valid @RequestBody(required = false) RiskEventResolutionRequest request) {
    return riskEventService.resolve(userDetails, eventId, request);
  }

  @PostMapping("/api/v1/admin/risk-events/{eventId}/false-positive")
  public RiskEventResponse falsePositive(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String eventId,
      @Valid @RequestBody(required = false) RiskEventResolutionRequest request) {
    return riskEventService.markFalsePositive(userDetails, eventId, request);
  }
}
