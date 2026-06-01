package com.handynest.geo;

import com.handynest.common.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GeoQueryService {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    @Transactional(readOnly = true)
    public List<GeoCountryResponse> findCountries(String locale) {
        return countryRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(country -> toResponse(country, locale))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GeoRegionResponse> findRegions(String countryCode, String locale) {
        Country country = countryRepository.findByCodeIgnoreCase(countryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Country", countryCode));

        return regionRepository.findAllByCountryCodeIgnoreCaseOrderByNameRuAsc(country.getCode())
                .stream()
                .map(region -> toResponse(region, locale))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GeoCityResponse> findCities(String regionPublicId, String locale) {
        Region region = regionRepository.findByPublicId(regionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Region", regionPublicId));

        return cityRepository.findAllByRegionPublicIdOrderBySortOrderAscIdAsc(region.getPublicId())
                .stream()
                .map(city -> toResponse(city, locale))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GeoDistrictResponse> findDistricts(String cityPublicId, String locale) {
        City city = cityRepository.findByPublicId(cityPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityPublicId));

        return districtRepository.findAllByCityPublicIdOrderByNameRuAsc(city.getPublicId())
                .stream()
                .map(district -> toResponse(district, locale))
                .toList();
    }

    private GeoCountryResponse toResponse(Country country, String locale) {
        return new GeoCountryResponse(
                country.getCode(),
                localizedName(country.getNameRu(), country.getNameKz(), country.getNameEn(), locale),
                country.getPhoneCode(),
                country.getCurrencyCode(),
                country.isSupported()
        );
    }

    private GeoRegionResponse toResponse(Region region, String locale) {
        return new GeoRegionResponse(
                region.getPublicId(),
                localizedName(region.getNameRu(), region.getNameKz(), region.getNameEn(), locale),
                region.getSlug(),
                region.isSupported()
        );
    }

    private GeoCityResponse toResponse(City city, String locale) {
        return new GeoCityResponse(
                city.getPublicId(),
                localizedName(city.getNameRu(), city.getNameKz(), city.getNameEn(), locale),
                city.getSlug(),
                city.getLatitude(),
                city.getLongitude(),
                city.isSupported(),
                city.getSortOrder()
        );
    }

    private GeoDistrictResponse toResponse(District district, String locale) {
        return new GeoDistrictResponse(
                district.getPublicId(),
                localizedName(district.getNameRu(), district.getNameKz(), district.getNameEn(), locale),
                district.getSlug(),
                district.getLatitude(),
                district.getLongitude(),
                district.isSupported()
        );
    }

    private String localizedName(String nameRu, String nameKz, String nameEn, String locale) {
        return switch (locale) {
            case "kk" -> nameKz;
            case "en" -> nameEn;
            default -> nameRu;
        };
    }
}
