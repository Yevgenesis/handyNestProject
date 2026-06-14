package com.handynest.market;

import com.handynest.common.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/market")
@Tag(name = "Market", description = "Active launch-market configuration")
public class MarketConfigApiV1Controller {

  private final MarketConfigService marketConfigService;

  @GetMapping("/config")
  @Operation(
      operationId = "getMarketConfig",
      summary = "Get active market defaults",
      description = "Returns the default country, city, currency and active frontend locales.")
  public MarketConfigResponse config() {
    return marketConfigService.current();
  }
}
