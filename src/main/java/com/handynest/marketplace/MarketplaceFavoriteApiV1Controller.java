package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my/favorite-performers")
@Tag(name = "Marketplace Favorites", description = "Authenticated user's favorite performer list.")
public class MarketplaceFavoriteApiV1Controller {

  private final MarketplaceService marketplaceService;

  @GetMapping
  @Operation(
      operationId = "listMyFavoritePerformers",
      summary = "List favorite performers",
      description = "Returns the authenticated user's favorite performer profiles.")
  public List<FavoritePerformerResponse> myFavorites(
      @AuthenticationPrincipal UserDetails userDetails) {
    return marketplaceService.myFavoritePerformers(userDetails);
  }

  @PostMapping("/{performerId}")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      operationId = "addFavoritePerformer",
      summary = "Add favorite performer",
      description = "Adds a performer profile publicId to the authenticated user's favorites.")
  public FavoritePerformerResponse addFavorite(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Performer profile publicId.") @PathVariable String performerId,
      @Valid @RequestBody(required = false) FavoritePerformerRequest request) {
    return marketplaceService.addFavoritePerformer(
        userDetails, performerId, request == null ? new FavoritePerformerRequest(null) : request);
  }

  @DeleteMapping("/{performerId}")
  @Operation(
      operationId = "removeFavoritePerformer",
      summary = "Remove favorite performer",
      description = "Removes a performer profile publicId from the authenticated user's favorites.")
  public ResponseEntity<Void> removeFavorite(
      @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Performer profile publicId.") @PathVariable String performerId) {
    marketplaceService.removeFavoritePerformer(userDetails, performerId);
    return ResponseEntity.noContent().build();
  }
}
