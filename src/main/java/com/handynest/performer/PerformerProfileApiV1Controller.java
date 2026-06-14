package com.handynest.performer;

import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.common.api.ApiConstants;
import com.handynest.common.api.PageResponse;
import com.handynest.common.i18n.SupportedLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping(ApiConstants.API_V1 + "/performers")
@Tag(name = "Performers", description = "Performer profiles")
public class PerformerProfileApiV1Controller {

  private final PerformerProfileService performerProfileService;

  @PostMapping("/me")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create current user's performer profile")
  public PerformerProfileResponse createMe(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody PerformerProfileRequest request,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.createMe(
        userDetails, request, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user's performer profile")
  public PerformerProfileResponse me(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.me(
        userDetails, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @PatchMapping("/me")
  @Operation(summary = "Update current user's performer profile")
  public PerformerProfileResponse updateMe(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody PerformerProfileRequest request,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.updateMe(
        userDetails, request, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @PostMapping("/me/availability")
  @Operation(summary = "Update current user's performer availability")
  public PerformerProfileResponse updateAvailability(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody PerformerAvailabilityRequest request,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.updateAvailability(
        userDetails, request, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/{performerId}")
  @Operation(summary = "Get public performer profile")
  public PerformerProfileResponse findById(
      @PathVariable String performerId,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.findByPublicId(
        performerId, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping
  @Operation(summary = "Search public performer profiles")
  public PageResponse<PerformerProfileResponse> search(
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) String cityId,
      @RequestParam(required = false) String districtId,
      @RequestParam(required = false) String countryCode,
      @RequestParam(required = false) CategoryServiceMode serviceMode,
      @RequestParam(required = false) PerformerVerificationLevel verificationLevel,
      @RequestParam(required = false) Boolean isAvailable,
      @RequestParam(required = false) Boolean isTopPerformer,
      @RequestParam(required = false) BigDecimal ratingMin,
      @RequestParam(required = false) BigDecimal priceMin,
      @RequestParam(required = false) BigDecimal priceMax,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return performerProfileService.search(
        categoryId,
        cityId,
        districtId,
        countryCode,
        serviceMode,
        verificationLevel,
        isAvailable,
        isTopPerformer,
        ratingMin,
        priceMin,
        priceMax,
        page,
        size,
        SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }
}
