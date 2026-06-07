package com.handynest.marketplace;

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
public class MarketplaceFavoriteApiV1Controller {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public List<FavoritePerformerResponse> myFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.myFavoritePerformers(userDetails);
    }

    @PostMapping("/{performerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoritePerformerResponse addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String performerId,
            @Valid @RequestBody(required = false) FavoritePerformerRequest request
    ) {
        return marketplaceService.addFavoritePerformer(
                userDetails,
                performerId,
                request == null ? new FavoritePerformerRequest(null) : request
        );
    }

    @DeleteMapping("/{performerId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String performerId
    ) {
        marketplaceService.removeFavoritePerformer(userDetails, performerId);
        return ResponseEntity.noContent().build();
    }
}
