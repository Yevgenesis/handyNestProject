package com.handynest.market;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Active launch-market defaults used by public clients.")
public record MarketConfigResponse(
    String defaultCountryCode,
    String defaultCityId,
    String defaultCityName,
    String defaultCurrency,
    String defaultLocale,
    List<String> supportedLocales) {

  public MarketConfigResponse {
    supportedLocales = List.copyOf(supportedLocales);
  }
}
