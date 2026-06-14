package com.handynest.geo;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.i18n.SupportedLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/geo")
@Tag(name = "Geo", description = "Public multi-country geo directory")
public class GeoApiV1Controller {

  private final GeoQueryService geoQueryService;

  @GetMapping("/countries")
  @Operation(summary = "List supported countries")
  public List<GeoCountryResponse> findCountries(
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return geoQueryService.findCountries(SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/countries/{countryCode}/regions")
  @Operation(summary = "List regions by country code")
  public List<GeoRegionResponse> findRegions(
      @PathVariable String countryCode,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return geoQueryService.findRegions(
        countryCode, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/regions/{regionId}/cities")
  @Operation(summary = "List cities by region public id")
  public List<GeoCityResponse> findCities(
      @PathVariable String regionId,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return geoQueryService.findCities(
        regionId, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/cities")
  @Operation(
      operationId = "listSupportedCities",
      summary = "List all supported cities",
      description = "Returns supported cities in one request for public forms and filters.")
  public List<GeoCityResponse> findSupportedCities(
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return geoQueryService.findSupportedCities(
        SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }

  @GetMapping("/cities/{cityId}/districts")
  @Operation(summary = "List districts by city public id")
  public List<GeoDistrictResponse> findDistricts(
      @PathVariable String cityId,
      @RequestParam(required = false) String locale,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
    return geoQueryService.findDistricts(
        cityId, SupportedLocaleResolver.resolve(locale, acceptLanguage));
  }
}
