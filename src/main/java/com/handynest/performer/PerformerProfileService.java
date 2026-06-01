package com.handynest.performer;

import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.User;
import com.handynest.catalog.category.CategoryQueryRepository;
import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.catalog.category.CategoryTranslation;
import com.handynest.catalog.category.CategoryTranslationRepository;
import com.handynest.catalog.category.RequiredVerificationLevel;
import com.handynest.common.api.PageResponse;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.error.VerificationRequiredException;
import com.handynest.geo.City;
import com.handynest.geo.CityRepository;
import com.handynest.geo.Country;
import com.handynest.geo.CountryRepository;
import com.handynest.geo.District;
import com.handynest.geo.DistrictRepository;
import com.handynest.identity.UserProfileService;
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

    private static final String DEFAULT_COUNTRY_CODE = "KZ";
    private static final String DEFAULT_CURRENCY = "KZT";

    private final PerformerProfileRepository performerProfileRepository;
    private final UserProfileService userProfileService;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final CategoryQueryRepository categoryRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public PerformerProfileResponse createMe(
            UserDetails userDetails,
            PerformerProfileRequest request,
            String locale
    ) {
        User user = userProfileService.currentUser(userDetails);
        if (performerProfileRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Performer profile already exists");
        }

        PerformerProfile profile = new PerformerProfile(user);
        applyProfileRequest(profile, request, true);
        profile = performerProfileRepository.save(profile);
        return toResponse(profile, locale);
    }

    @Transactional(readOnly = true)
    public PerformerProfileResponse me(UserDetails userDetails, String locale) {
        User user = userProfileService.currentUser(userDetails);
        PerformerProfile profile = performerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
        return toResponse(profile, locale);
    }

    @Transactional
    public PerformerProfileResponse updateMe(
            UserDetails userDetails,
            PerformerProfileRequest request,
            String locale
    ) {
        User user = userProfileService.currentUser(userDetails);
        PerformerProfile profile = performerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
        applyProfileRequest(profile, request, false);
        return toResponse(profile, locale);
    }

    @Transactional
    public PerformerProfileResponse updateAvailability(
            UserDetails userDetails,
            PerformerAvailabilityRequest request,
            String locale
    ) {
        User user = userProfileService.currentUser(userDetails);
        PerformerProfile profile = performerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
        profile.setAvailable(Boolean.TRUE.equals(request.available()));
        return toResponse(profile, locale);
    }

    @Transactional(readOnly = true)
    public PerformerProfileResponse findByPublicId(String performerId, String locale) {
        PerformerProfile profile = performerProfileRepository.findByPublicId(performerId)
                .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", performerId));
        return toResponse(profile, locale);
    }

    @Transactional(readOnly = true)
    public PageResponse<PerformerProfileResponse> search(
            String categoryId,
            String cityId,
            String districtId,
            CategoryServiceMode serviceMode,
            PerformerVerificationLevel verificationLevel,
            Boolean isAvailable,
            Boolean isTopPerformer,
            BigDecimal ratingMin,
            BigDecimal priceMin,
            BigDecimal priceMax,
            int page,
            int size,
            String locale
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by("createdAt").descending());

        return PageResponse.from(performerProfileRepository
                .findAll(searchSpec(
                        categoryId,
                        cityId,
                        districtId,
                        serviceMode,
                        verificationLevel,
                        isAvailable,
                        isTopPerformer,
                        ratingMin,
                        priceMin,
                        priceMax
                ), pageable)
                .map(profile -> toResponse(profile, locale)));
    }

    private void applyProfileRequest(
            PerformerProfile profile,
            PerformerProfileRequest request,
            boolean creating
    ) {
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
            profile.setBaseCountry(findCountry(
                    request.countryCode() == null ? DEFAULT_COUNTRY_CODE : request.countryCode()
            ));
        }
        if (creating || request.cityId() != null) {
            if (request.cityId() == null || request.cityId().isBlank()) {
                throw new BadRequestBusinessException("baseCityId is required");
            }
            City city = findCity(request.cityId());
            profile.setBaseCity(city);
            profile.setBaseRegion(city.getRegion());
            profile.setBaseCountry(city.getCountry());
        }
        if (request.districtId() != null) {
            District district = findDistrict(request.districtId());
            profile.setBaseDistrict(district);
            profile.setBaseCity(district.getCity());
            profile.setBaseRegion(district.getCity().getRegion());
            profile.setBaseCountry(district.getCity().getCountry());
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
            Set<PerformerCategory> categories = buildCategories(profile, request.categories());
            if (!creating) {
                profile.getCategories().clear();
                entityManager.flush();
            }
            profile.replaceCategories(categories);
        }
    }

    private Set<PerformerCategory> buildCategories(
            PerformerProfile profile,
            List<PerformerCategoryRequest> categoryRequests
    ) {
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
        boolean primarySeen = categoryRequests.stream().anyMatch(request -> Boolean.TRUE.equals(request.primary()));
        for (int i = 0; i < categoryRequests.size(); i++) {
            PerformerCategoryRequest request = categoryRequests.get(i);
            Category category = categoryRepository.findByPublicId(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            assertVerificationAllowed(profile, category);
            assertPriceRange(request);

            PerformerCategory performerCategory = new PerformerCategory(category);
            performerCategory.setExperienceYears(request.experienceYears());
            performerCategory.setPriceFrom(request.priceFrom());
            performerCategory.setPriceTo(request.priceTo());
            performerCategory.setCurrency(normalizeCurrency(request.currency()));
            performerCategory.setPrimaryCategory(primarySeen ? Boolean.TRUE.equals(request.primary()) : i == 0);
            categories.add(performerCategory);
        }
        return categories;
    }

    private void assertVerificationAllowed(PerformerProfile profile, Category category) {
        if (verificationRank(profile.getVerificationLevel()) < verificationRank(category.getRequiresVerificationLevel())) {
            throw new VerificationRequiredException("Category requires " + category.getRequiresVerificationLevel());
        }
    }

    private int verificationRank(PerformerVerificationLevel level) {
        return switch (level) {
            case NONE -> 0;
            case PHONE_VERIFIED -> 1;
            case ID_VERIFIED -> 2;
            case PAYMENT_VERIFIED -> 3;
            case BUSINESS_VERIFIED -> 4;
        };
    }

    private int verificationRank(RequiredVerificationLevel level) {
        return switch (level) {
            case NONE -> 0;
            case PHONE_VERIFIED -> 1;
            case ID_VERIFIED -> 2;
            case BUSINESS_VERIFIED -> 4;
        };
    }

    private Specification<PerformerProfile> searchSpec(
            String categoryId,
            String cityId,
            String districtId,
            CategoryServiceMode serviceMode,
            PerformerVerificationLevel verificationLevel,
            Boolean isAvailable,
            Boolean isTopPerformer,
            BigDecimal ratingMin,
            BigDecimal priceMin,
            BigDecimal priceMax
    ) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            Join<PerformerProfile, PerformerCategory> categoryJoin = null;

            if (categoryId != null || priceMin != null || priceMax != null) {
                categoryJoin = root.join("categories", JoinType.INNER);
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(categoryJoin.get("category").get("publicId"), categoryId));
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
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(categoryJoin.get("priceFrom"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(categoryJoin.get("priceTo"), priceMax));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private PerformerProfileResponse toResponse(PerformerProfile profile, String locale) {
        Map<Long, String> categoryTitles = localizedCategoryTitles(profile, locale);
        return new PerformerProfileResponse(
                profile.getPublicId(),
                profile.getDisplayName(),
                profile.getDescription(),
                profile.getSkillsDescription(),
                profile.getBaseCountry().getCode(),
                profile.getBaseCountry().getNameRu(),
                profile.getBaseRegion() == null ? null : profile.getBaseRegion().getPublicId(),
                profile.getBaseRegion() == null ? null : profile.getBaseRegion().getNameRu(),
                profile.getBaseCity().getPublicId(),
                profile.getBaseCity().getNameRu(),
                profile.getBaseDistrict() == null ? null : profile.getBaseDistrict().getPublicId(),
                profile.getBaseDistrict() == null ? null : profile.getBaseDistrict().getNameRu(),
                profile.getServiceRadiusKm(),
                profile.isWorksRemotely(),
                profile.isWorksOnsite(),
                profile.getTravelFeePolicy(),
                profile.getVerificationLevel().name(),
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
                profile.getCategories().stream()
                        .map(category -> toResponse(category, categoryTitles))
                        .toList()
        );
    }

    private PerformerCategoryResponse toResponse(
            PerformerCategory performerCategory,
            Map<Long, String> categoryTitles
    ) {
        Category category = performerCategory.getCategory();
        return new PerformerCategoryResponse(
                category.getPublicId(),
                categoryTitles.getOrDefault(category.getId(), category.getTitle()),
                performerCategory.getExperienceYears(),
                performerCategory.getPriceFrom(),
                performerCategory.getPriceTo(),
                performerCategory.getCurrency(),
                performerCategory.isPrimaryCategory()
        );
    }

    private Map<Long, String> localizedCategoryTitles(PerformerProfile profile, String locale) {
        List<Long> categoryIds = profile.getCategories().stream()
                .map(category -> category.getCategory().getId())
                .toList();
        Map<Long, String> titles = new HashMap<>();
        categoryTranslationRepository.findByCategoryIdInAndLocaleIn(categoryIds, List.of(locale, "ru"))
                .forEach(translation -> putLocalizedTitle(titles, translation, locale));
        return titles;
    }

    private void putLocalizedTitle(Map<Long, String> titles, CategoryTranslation translation, String locale) {
        Long categoryId = translation.getCategory().getId();
        if ("ru".equals(translation.getLocale())) {
            titles.putIfAbsent(categoryId, translation.getName());
        }
        if (locale.equals(translation.getLocale())) {
            titles.put(categoryId, translation.getName());
        }
    }

    private Country findCountry(String countryCode) {
        return countryRepository.findByCodeIgnoreCase(countryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Country", countryCode));
    }

    private City findCity(String cityPublicId) {
        return cityRepository.findByPublicId(cityPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityPublicId));
    }

    private District findDistrict(String districtPublicId) {
        return districtRepository.findByPublicId(districtPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("District", districtPublicId));
    }

    private void assertPriceRange(PerformerCategoryRequest request) {
        if (request.priceFrom() != null
                && request.priceTo() != null
                && request.priceFrom().compareTo(request.priceTo()) > 0) {
            throw new BadRequestBusinessException("priceFrom must be less than or equal to priceTo");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
