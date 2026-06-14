package com.handynest.performer;

import com.handynest.catalog.category.Category;
import com.handynest.catalog.category.CategoryAccessPolicy;
import com.handynest.catalog.category.CategoryQueryRepository;
import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.catalog.category.CategoryTranslation;
import com.handynest.catalog.category.CategoryTranslationRepository;
import com.handynest.common.api.PageResponse;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.geo.City;
import com.handynest.geo.CityRepository;
import com.handynest.geo.Country;
import com.handynest.geo.CountryRepository;
import com.handynest.geo.District;
import com.handynest.geo.DistrictRepository;
import com.handynest.identity.ConsentType;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserConsentService;
import com.handynest.identity.UserProfileService;
import com.handynest.market.MarketConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformerProfileService {

  private final PerformerProfileRepository performerProfileRepository;
  private final UserProfileService userProfileService;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final DistrictRepository districtRepository;
  private final CategoryQueryRepository categoryRepository;
  private final CategoryTranslationRepository categoryTranslationRepository;
  private final CategoryAccessPolicy categoryAccessPolicy;
  private final UserConsentService userConsentService;
  private final MarketConfigService marketConfigService;

  @PersistenceContext private EntityManager entityManager;

  @Transactional
  public PerformerProfileResponse createMe(
      UserDetails userDetails, PerformerProfileRequest request, String locale) {
    User user = userProfileService.currentUser(userDetails);
    userConsentService.requireCurrent(user, ConsentType.PERFORMER_RULES);
    userConsentService.requireCurrent(user, ConsentType.PROHIBITED_SERVICES_POLICY);
    if (performerProfileRepository.existsByUserId(user.getId())) {
      throw new DuplicateResourceException("Performer profile already exists");
    }

    PerformerProfile profile = new PerformerProfile(user);
    if (user.isPhoneVerified()) {
      profile.markPhoneVerified(java.time.Instant.now());
    }
    applyProfileRequest(profile, request, true);
    profile = performerProfileRepository.save(profile);
    user.getRoles().add(RoleName.PERFORMER);
    return toResponse(profile, locale, true);
  }

  @Transactional(readOnly = true)
  public PerformerProfileResponse me(UserDetails userDetails, String locale) {
    User user = userProfileService.currentUser(userDetails);
    PerformerProfile profile =
        performerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
    return toResponse(profile, locale, true);
  }

  @Transactional
  public PerformerProfileResponse updateMe(
      UserDetails userDetails, PerformerProfileRequest request, String locale) {
    User user = userProfileService.currentUser(userDetails);
    PerformerProfile profile =
        performerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
    applyProfileRequest(profile, request, false);
    return toResponse(profile, locale, true);
  }

  @Transactional
  public PerformerProfileResponse updateAvailability(
      UserDetails userDetails, PerformerAvailabilityRequest request, String locale) {
    User user = userProfileService.currentUser(userDetails);
    PerformerProfile profile =
        performerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
    profile.setAvailable(Boolean.TRUE.equals(request.available()));
    return toResponse(profile, locale, true);
  }

  @Transactional(readOnly = true)
  public PerformerProfileResponse findByPublicId(String performerId, String locale) {
    PerformerProfile profile =
        performerProfileRepository
            .findByPublicId(performerId)
            .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", performerId));
    assertPubliclySupported(profile);
    return toResponse(profile, locale, false);
  }

  @Transactional(readOnly = true)
  public PageResponse<PerformerProfileResponse> search(
      String categoryId,
      String cityId,
      String districtId,
      String countryCode,
      CategoryServiceMode serviceMode,
      PerformerVerificationLevel verificationLevel,
      Boolean isAvailable,
      Boolean isTopPerformer,
      BigDecimal ratingMin,
      BigDecimal priceMin,
      BigDecimal priceMax,
      int page,
      int size,
      String locale) {
    int safeSize = Math.min(Math.max(size, 1), 100);
    Pageable pageable =
        PageRequest.of(Math.max(page, 0), safeSize, Sort.by("createdAt").descending());

    return PageResponse.from(
        performerProfileRepository
            .findAll(
                searchSpec(
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
                    priceMax),
                pageable)
            .map(profile -> toResponse(profile, locale, false)));
  }

  private void applyProfileRequest(
      PerformerProfile profile, PerformerProfileRequest request, boolean creating) {
    if (creating || request.displayName() != null) {
      if (request.displayName() == null || request.displayName().isBlank()) {
        throw new BadRequestBusinessException("displayName is required");
      }
      profile.setDisplayName(request.displayName().trim());
    }
    if (request.description() != null) {
      profile.setDescription(blankToNull(request.description()));
    }
    if (request.skillsDescription() != null) {
      profile.setSkillsDescription(blankToNull(request.skillsDescription()));
    }
    if (creating || request.countryCode() != null) {
      profile.setBaseCountry(
          findCountry(
              request.countryCode() == null
                  ? marketConfigService.defaultCountry().getCode()
                  : request.countryCode()));
    }
    if (creating || request.cityId() != null) {
      if (request.cityId() == null || request.cityId().isBlank()) {
        throw new BadRequestBusinessException("baseCityId is required");
      }
      City city = findCity(request.cityId());
      assertSupportedCity(city);
      profile.setBaseDistrict(null);
      profile.setBaseCity(city);
      profile.setBaseRegion(city.getRegion());
      profile.setBaseCountry(city.getCountry());
    }
    if (request.districtId() != null) {
      if (request.districtId().isBlank()) {
        profile.setBaseDistrict(null);
      } else {
        District district = findDistrict(request.districtId());
        if (!district.isSupported()) {
          throw new BadRequestBusinessException("Selected district is not supported");
        }
        profile.setBaseDistrict(district);
        profile.setBaseCity(district.getCity());
        profile.setBaseRegion(district.getCity().getRegion());
        profile.setBaseCountry(district.getCity().getCountry());
      }
    }
    if (creating || request.serviceRadiusKm() != null) {
      profile.setServiceRadiusKm(request.serviceRadiusKm() == null ? 0 : request.serviceRadiusKm());
    }
    if (creating || request.worksRemotely() != null) {
      profile.setWorksRemotely(Boolean.TRUE.equals(request.worksRemotely()));
    }
    if (creating || request.worksOnsite() != null) {
      profile.setWorksOnsite(Boolean.TRUE.equals(request.worksOnsite()));
    }
    if (!profile.isWorksRemotely() && !profile.isWorksOnsite()) {
      throw new BadRequestBusinessException("At least one service mode must be enabled");
    }
    if (request.travelFeePolicy() != null) {
      profile.setTravelFeePolicy(blankToNull(request.travelFeePolicy()));
    }
    if (creating) {
      profile.setAvailable(true);
    }
    if (creating || request.categories() != null) {
      Map<Long, PerformerCategory> previousCategories =
          profile.getCategories().stream()
              .collect(
                  java.util.stream.Collectors.toMap(
                      category -> category.getCategory().getId(), category -> category));
      Set<PerformerCategory> categories =
          buildCategories(profile, request.categories(), previousCategories);
      if (!creating) {
        profile.getCategories().clear();
        entityManager.flush();
      }
      profile.replaceCategories(categories);
    }
  }

  private Set<PerformerCategory> buildCategories(
      PerformerProfile profile,
      List<PerformerCategoryRequest> categoryRequests,
      Map<Long, PerformerCategory> previousCategories) {
    if (categoryRequests == null || categoryRequests.isEmpty()) {
      throw new BadRequestBusinessException("At least one category is required");
    }

    Set<String> categoryPublicIds = new LinkedHashSet<>();
    for (PerformerCategoryRequest request : categoryRequests) {
      if (!categoryPublicIds.add(request.categoryId())) {
        throw new BadRequestBusinessException("Duplicate category: " + request.categoryId());
      }
    }

    Set<PerformerCategory> categories = new LinkedHashSet<>();
    boolean primarySeen =
        categoryRequests.stream().anyMatch(request -> Boolean.TRUE.equals(request.primary()));
    for (int i = 0; i < categoryRequests.size(); i++) {
      PerformerCategoryRequest request = categoryRequests.get(i);
      Category category =
          categoryRepository
              .findByPublicId(request.categoryId())
              .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
      categoryAccessPolicy.assertPerformerCanServeCategory(profile, category);
      assertPriceRange(request);

      PerformerCategory performerCategory = new PerformerCategory(category);
      PerformerCategory previous = previousCategories.get(category.getId());
      if (previous != null) {
        performerCategory.preserveModerationFrom(previous);
      }
      performerCategory.setExperienceYears(request.experienceYears());
      performerCategory.setPriceFrom(request.priceFrom());
      performerCategory.setPriceTo(request.priceTo());
      performerCategory.setCurrency(profile.getBaseCountry().getCurrencyCode());
      performerCategory.setPrimaryCategory(
          primarySeen ? Boolean.TRUE.equals(request.primary()) : i == 0);
      categories.add(performerCategory);
    }
    return categories;
  }

  private Specification<PerformerProfile> searchSpec(
      String categoryId,
      String cityId,
      String districtId,
      String countryCode,
      CategoryServiceMode serviceMode,
      PerformerVerificationLevel verificationLevel,
      Boolean isAvailable,
      Boolean isTopPerformer,
      BigDecimal ratingMin,
      BigDecimal priceMin,
      BigDecimal priceMax) {
    return (root, query, criteriaBuilder) -> {
      if (query != null) {
        query.distinct(true);
      }
      List<Predicate> predicates = new ArrayList<>();
      String effectiveCountryCode =
          countryCode == null || countryCode.isBlank()
              ? marketConfigService.defaultCountry().getCode()
              : countryCode.trim().toUpperCase(Locale.ROOT);
      predicates.add(
          criteriaBuilder.equal(root.get("baseCountry").get("code"), effectiveCountryCode));
      predicates.add(criteriaBuilder.isTrue(root.get("baseCountry").get("supported")));
      predicates.add(criteriaBuilder.isTrue(root.get("baseCity").get("supported")));
      Join<PerformerProfile, PerformerCategory> categoryJoin = null;

      if (categoryId != null || priceMin != null || priceMax != null) {
        categoryJoin = root.join("categories", JoinType.INNER);
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.equal(
                    categoryJoin.get("approvalStatus"), PerformerCategoryApprovalStatus.APPROVED),
                criteriaBuilder.and(
                    criteriaBuilder.equal(
                        categoryJoin.get("approvalStatus"),
                        PerformerCategoryApprovalStatus.NOT_REQUIRED),
                    criteriaBuilder.isFalse(
                        categoryJoin.get("category").get("requiresManualApproval")),
                    criteriaBuilder.isFalse(categoryJoin.get("category").get("requiresLicense")))));
      }
      if (categoryId != null) {
        predicates.add(
            criteriaBuilder.equal(categoryJoin.get("category").get("publicId"), categoryId));
      }
      if (cityId != null) {
        predicates.add(criteriaBuilder.equal(root.get("baseCity").get("publicId"), cityId));
      }
      if (districtId != null) {
        predicates.add(criteriaBuilder.equal(root.get("baseDistrict").get("publicId"), districtId));
      }
      if (serviceMode != null) {
        switch (serviceMode) {
          case REMOTE -> predicates.add(criteriaBuilder.isTrue(root.get("worksRemotely")));
          case ONSITE -> predicates.add(criteriaBuilder.isTrue(root.get("worksOnsite")));
          case HYBRID -> {
            predicates.add(criteriaBuilder.isTrue(root.get("worksRemotely")));
            predicates.add(criteriaBuilder.isTrue(root.get("worksOnsite")));
          }
        }
      }
      if (verificationLevel != null) {
        predicates.add(criteriaBuilder.equal(root.get("verificationLevel"), verificationLevel));
        if (verificationLevel != PerformerVerificationLevel.NONE) {
          predicates.add(criteriaBuilder.isTrue(root.get("user").get("phoneVerified")));
        }
      }
      if (isAvailable != null) {
        predicates.add(criteriaBuilder.equal(root.get("available"), isAvailable));
      }
      if (isTopPerformer != null) {
        predicates.add(criteriaBuilder.equal(root.get("topPerformer"), isTopPerformer));
      }
      if (ratingMin != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ratingAverage"), ratingMin));
      }
      if (priceMin != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(categoryJoin.get("priceFrom"), priceMin));
      }
      if (priceMax != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(categoryJoin.get("priceTo"), priceMax));
      }

      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private PerformerProfileResponse toResponse(
      PerformerProfile profile, String locale, boolean includeRestrictedCategories) {
    List<PerformerCategory> visibleCategories =
        profile.getCategories().stream()
            .filter(category -> includeRestrictedCategories || category.isApprovedForServing())
            .toList();
    Map<Long, String> categoryTitles = localizedCategoryTitles(visibleCategories, locale);
    return new PerformerProfileResponse(
        profile.getPublicId(),
        profile.getDisplayName(),
        profile.getDescription(),
        profile.getSkillsDescription(),
        profile.getBaseCountry().getCode(),
        localizedGeoName(
            profile.getBaseCountry().getNameRu(),
            profile.getBaseCountry().getNameUz(),
            profile.getBaseCountry().getNameKz(),
            profile.getBaseCountry().getNameEn(),
            locale),
        profile.getBaseRegion() == null ? null : profile.getBaseRegion().getPublicId(),
        profile.getBaseRegion() == null
            ? null
            : localizedGeoName(
                profile.getBaseRegion().getNameRu(),
                profile.getBaseRegion().getNameUz(),
                profile.getBaseRegion().getNameKz(),
                profile.getBaseRegion().getNameEn(),
                locale),
        profile.getBaseCity().getPublicId(),
        localizedGeoName(
            profile.getBaseCity().getNameRu(),
            profile.getBaseCity().getNameUz(),
            profile.getBaseCity().getNameKz(),
            profile.getBaseCity().getNameEn(),
            locale),
        profile.getBaseDistrict() == null ? null : profile.getBaseDistrict().getPublicId(),
        profile.getBaseDistrict() == null
            ? null
            : localizedGeoName(
                profile.getBaseDistrict().getNameRu(),
                profile.getBaseDistrict().getNameUz(),
                profile.getBaseDistrict().getNameKz(),
                profile.getBaseDistrict().getNameEn(),
                locale),
        profile.getServiceRadiusKm(),
        profile.isWorksRemotely(),
        profile.isWorksOnsite(),
        profile.getTravelFeePolicy(),
        profile.getEffectiveVerificationLevel().name(),
        profile.getVerificationStatus().name(),
        profile.getRatingAverage(),
        profile.getRatingCount(),
        profile.getCompletedTasksCount(),
        profile.getCanceledTasksCount(),
        profile.getDisputeCount(),
        profile.isAvailable(),
        profile.isTopPerformer(),
        profile.getRejectedAt(),
        profile.getRejectionReason(),
        visibleCategories.stream().map(category -> toResponse(category, categoryTitles)).toList());
  }

  private PerformerCategoryResponse toResponse(
      PerformerCategory performerCategory, Map<Long, String> categoryTitles) {
    Category category = performerCategory.getCategory();
    return new PerformerCategoryResponse(
        category.getPublicId(),
        categoryTitles.getOrDefault(category.getId(), category.getTitle()),
        performerCategory.getExperienceYears(),
        performerCategory.getPriceFrom(),
        performerCategory.getPriceTo(),
        performerCategory.getCurrency(),
        performerCategory.isPrimaryCategory(),
        performerCategory.getApprovalStatus(),
        performerCategory.getRejectionReason());
  }

  private Map<Long, String> localizedCategoryTitles(
      List<PerformerCategory> categories, String locale) {
    List<Long> categoryIds =
        categories.stream().map(category -> category.getCategory().getId()).toList();
    if (categoryIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, String> titles = new HashMap<>();
    categoryTranslationRepository
        .findByCategoryIdInAndLocaleIn(categoryIds, List.of(locale, "ru"))
        .forEach(translation -> putLocalizedTitle(titles, translation, locale));
    return titles;
  }

  private void putLocalizedTitle(
      Map<Long, String> titles, CategoryTranslation translation, String locale) {
    Long categoryId = translation.getCategory().getId();
    if ("ru".equals(translation.getLocale())) {
      titles.putIfAbsent(categoryId, translation.getName());
    }
    if (locale.equals(translation.getLocale())) {
      titles.put(categoryId, translation.getName());
    }
  }

  private Country findCountry(String countryCode) {
    return countryRepository
        .findByCodeIgnoreCase(countryCode)
        .orElseThrow(() -> new ResourceNotFoundException("Country", countryCode));
  }

  private City findCity(String cityPublicId) {
    return cityRepository
        .findByPublicId(cityPublicId)
        .orElseThrow(() -> new ResourceNotFoundException("City", cityPublicId));
  }

  private District findDistrict(String districtPublicId) {
    return districtRepository
        .findByPublicId(districtPublicId)
        .orElseThrow(() -> new ResourceNotFoundException("District", districtPublicId));
  }

  private void assertPriceRange(PerformerCategoryRequest request) {
    if (request.priceFrom() != null
        && request.priceTo() != null
        && request.priceFrom().compareTo(request.priceTo()) > 0) {
      throw new BadRequestBusinessException("priceFrom must be less than or equal to priceTo");
    }
  }

  private void assertSupportedCity(City city) {
    if (!city.isSupported() || !city.getCountry().isSupported()) {
      throw new BadRequestBusinessException("Selected city is not supported by the active market");
    }
  }

  private void assertPubliclySupported(PerformerProfile profile) {
    if (!profile.getBaseCountry().isSupported() || !profile.getBaseCity().isSupported()) {
      throw new ResourceNotFoundException("PerformerProfile", profile.getPublicId());
    }
  }

  private String localizedGeoName(String ru, String uz, String kk, String en, String locale) {
    return switch (locale) {
      case "uz" -> uz == null || uz.isBlank() ? ru : uz;
      case "kk" -> kk;
      case "en" -> en;
      default -> ru;
    };
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
