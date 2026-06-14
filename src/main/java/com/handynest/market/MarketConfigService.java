package com.handynest.market;

import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.geo.City;
import com.handynest.geo.CityRepository;
import com.handynest.geo.Country;
import com.handynest.geo.CountryRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketConfigService {

  private final PlatformSettingService platformSettingService;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;

  @Transactional(readOnly = true)
  public MarketConfigResponse current() {
    Country country = defaultCountry();
    City city = defaultCity();
    return new MarketConfigResponse(
        country.getCode(),
        city.getPublicId(),
        city.getNameRu(),
        country.getCurrencyCode(),
        defaultLocale(),
        supportedLocales());
  }

  @Transactional(readOnly = true)
  public Country defaultCountry() {
    String countryCode =
        platformSettingService.stringValue(PlatformSettingKey.MARKET_DEFAULT_COUNTRY_CODE);
    return countryRepository
        .findByCodeIgnoreCase(countryCode)
        .filter(Country::isSupported)
        .orElseThrow(() -> new ResourceNotFoundException("Country", countryCode));
  }

  @Transactional(readOnly = true)
  public City defaultCity() {
    String cityId =
        platformSettingService.stringValue(PlatformSettingKey.MARKET_DEFAULT_CITY_PUBLIC_ID);
    return cityRepository
        .findByPublicId(cityId)
        .filter(City::isSupported)
        .orElseThrow(() -> new ResourceNotFoundException("City", cityId));
  }

  public String defaultLocale() {
    return platformSettingService.stringValue(PlatformSettingKey.MARKET_DEFAULT_LOCALE);
  }

  public List<String> supportedLocales() {
    return Arrays.stream(
            platformSettingService
                .stringValue(PlatformSettingKey.MARKET_SUPPORTED_LOCALES)
                .split(","))
        .map(String::trim)
        .map(value -> value.toLowerCase(Locale.ROOT))
        .filter(value -> !value.isBlank())
        .distinct()
        .toList();
  }
}
