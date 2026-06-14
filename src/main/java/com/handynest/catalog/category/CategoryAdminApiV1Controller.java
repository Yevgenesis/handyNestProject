package com.handynest.catalog.category;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/admin/categories")
@Tag(name = "Admin Categories", description = "Category catalog policy and localization management")
public class CategoryAdminApiV1Controller {

  private final CategoryAdminService categoryAdminService;

  @GetMapping
  @Operation(operationId = "adminListCategories", summary = "List all categories")
  public List<CategoryAdminResponse> list(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) CategoryRiskLevel riskLevel,
      @RequestParam(required = false) Boolean active) {
    return categoryAdminService.list(userDetails, riskLevel, active);
  }

  @GetMapping("/{categoryId}")
  @Operation(operationId = "adminGetCategory", summary = "Get complete category configuration")
  public CategoryAdminResponse find(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Category publicId") @PathVariable String categoryId) {
    return categoryAdminService.find(userDetails, categoryId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(operationId = "adminCreateCategory", summary = "Create a category")
  public CategoryAdminResponse create(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(ApiConstants.IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Valid @RequestBody CategoryAdminUpsertRequest request) {
    return categoryAdminService.create(userDetails, idempotencyKey, request);
  }

  @PutMapping("/{categoryId}")
  @Operation(operationId = "adminUpdateCategory", summary = "Replace category configuration")
  public CategoryAdminResponse update(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Category publicId") @PathVariable String categoryId,
      @Valid @RequestBody CategoryAdminUpsertRequest request) {
    return categoryAdminService.update(userDetails, categoryId, request);
  }

  @GetMapping("/{categoryId}/audit-events")
  @Operation(operationId = "adminListCategoryAuditEvents", summary = "List category audit history")
  public List<CategoryAuditEventResponse> auditEvents(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Category publicId") @PathVariable String categoryId) {
    return categoryAdminService.auditEvents(userDetails, categoryId);
  }
}
