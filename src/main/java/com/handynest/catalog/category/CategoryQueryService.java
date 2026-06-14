package com.handynest.catalog.category;

import com.handynest.common.error.ResourceNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

  private final CategoryQueryRepository categoryRepository;
  private final CategoryTranslationRepository translationRepository;
  private final CategorySynonymRepository synonymRepository;

  @Transactional(readOnly = true)
  public List<CategoryResponse> findTree(String locale) {
    return findTree(locale, null, null);
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> findTree(
      String locale, String query, CategoryServiceMode serviceMode) {
    List<Category> allCategories = categoryRepository.findAllByOrderBySortOrderAscIdAsc();
    List<Category> available = allCategories.stream().filter(this::isPubliclyAvailable).toList();
    List<Category> serviceFiltered = filterByServiceMode(available, serviceMode);
    Map<Long, LocalizedCategoryText> texts = loadLocalizedTexts(serviceFiltered, locale);
    List<Category> filtered = filterBySearch(serviceFiltered, texts, locale, query);
    return toTree(filtered, texts);
  }

  @Transactional(readOnly = true)
  public CategoryResponse findByPublicId(String publicId, String locale) {
    Category category =
        categoryRepository
            .findByPublicId(publicId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", publicId));
    if (!isPubliclyAvailable(category)) {
      throw new ResourceNotFoundException("Category", publicId);
    }
    List<Category> categories =
        categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .filter(this::isPubliclyAvailable)
            .toList();
    Map<Long, List<Category>> childrenByParentId = groupChildren(categories);

    return toResponse(category, childrenByParentId, loadLocalizedTexts(categories, locale));
  }

  private List<CategoryResponse> toTree(
      List<Category> categories, Map<Long, LocalizedCategoryText> localizedTexts) {
    Map<Long, List<Category>> childrenByParentId = groupChildren(categories);

    return categories.stream()
        .filter(category -> category.getParentId() == null)
        .sorted(categoryComparator())
        .map(category -> toResponse(category, childrenByParentId, localizedTexts))
        .toList();
  }

  private Map<Long, List<Category>> groupChildren(List<Category> categories) {
    return categories.stream()
        .filter(category -> category.getParentId() != null)
        .collect(Collectors.groupingBy(Category::getParentId));
  }

  private CategoryResponse toResponse(
      Category category,
      Map<Long, List<Category>> childrenByParentId,
      Map<Long, LocalizedCategoryText> localizedTexts) {
    List<CategoryResponse> children =
        childrenByParentId.getOrDefault(category.getId(), List.of()).stream()
            .sorted(categoryComparator())
            .map(child -> toResponse(child, childrenByParentId, localizedTexts))
            .toList();
    LocalizedCategoryText text =
        localizedTexts.getOrDefault(
            category.getId(), new LocalizedCategoryText(category.getTitle()));

    return new CategoryResponse(
        category.getPublicId(),
        text.name(),
        category.getSlug(),
        category.getIcon(),
        category.getRiskLevel().name(),
        category.getServiceMode().name(),
        category.getLaunchPhase().name(),
        category.isActive(),
        category.isPublicVisible(),
        category.getSortOrder(),
        category.getRequiresVerificationLevel().name(),
        category.isRequiresManualApproval(),
        category.isRequiresLicense(),
        category.isAllowsRemote(),
        category.isAllowsOnsite(),
        category.isAllowsEscrow(),
        category.isAllowsCash(),
        category.isAllowsMilestones(),
        category.isAllowsAttachments(),
        category.isAllowsContactReveal(),
        category.isRequiresOnsiteCoordination(),
        category.getContactRevealStage().name(),
        category.getWeight(),
        children);
  }

  private Comparator<Category> categoryComparator() {
    return Comparator.comparingInt(Category::getSortOrder).thenComparing(Category::getId);
  }

  private Map<Long, LocalizedCategoryText> loadLocalizedTexts(
      List<Category> categories, String locale) {
    if (categories.isEmpty()) {
      return Map.of();
    }
    List<Long> categoryIds = categories.stream().map(Category::getId).toList();
    List<CategoryTranslation> translations =
        translationRepository.findByCategoryIdInAndLocaleIn(categoryIds, List.of(locale, "ru"));

    Map<Long, LocalizedCategoryText> fallbackTexts = new HashMap<>();
    Map<Long, LocalizedCategoryText> requestedTexts = new HashMap<>();
    for (CategoryTranslation translation : translations) {
      Long categoryId = translation.getCategory().getId();
      LocalizedCategoryText text = new LocalizedCategoryText(translation.getName());
      if ("ru".equals(translation.getLocale())) {
        fallbackTexts.put(categoryId, text);
      }
      if (locale.equals(translation.getLocale())) {
        requestedTexts.put(categoryId, text);
      }
    }

    Map<Long, LocalizedCategoryText> localizedTexts = new HashMap<>(fallbackTexts);
    localizedTexts.putAll(requestedTexts);
    categories.forEach(
        category ->
            localizedTexts.putIfAbsent(
                category.getId(), new LocalizedCategoryText(category.getTitle())));

    return localizedTexts;
  }

  private List<Category> filterBySearch(
      List<Category> categories,
      Map<Long, LocalizedCategoryText> texts,
      String locale,
      String query) {
    if (query == null || query.isBlank()) {
      return categories;
    }
    String normalizedQuery = CategoryAdminService.normalizeSearchText(query);
    Set<Long> matchingIds =
        categories.stream()
            .filter(
                category ->
                    category.getSlug().contains(normalizedQuery)
                        || CategoryAdminService.normalizeSearchText(
                                texts
                                    .getOrDefault(
                                        category.getId(),
                                        new LocalizedCategoryText(category.getTitle()))
                                    .name())
                            .contains(normalizedQuery))
            .map(Category::getId)
            .collect(Collectors.toSet());

    List<Long> categoryIds = categories.stream().map(Category::getId).toList();
    synonymRepository.findByCategoryIdInAndLocaleIn(categoryIds, List.of(locale, "ru")).stream()
        .filter(synonym -> synonym.getNormalizedValue().contains(normalizedQuery))
        .map(synonym -> synonym.getCategory().getId())
        .forEach(matchingIds::add);

    Map<Long, Category> byId =
        categories.stream().collect(Collectors.toMap(Category::getId, category -> category));
    Set<Long> visibleIds = new HashSet<>(matchingIds);
    for (Long matchingId : matchingIds) {
      Category current = byId.get(matchingId);
      while (current != null && current.getParentId() != null) {
        Category parent = byId.get(current.getParentId());
        if (parent == null || !visibleIds.add(parent.getId())) {
          break;
        }
        current = parent;
      }
    }
    return categories.stream().filter(category -> visibleIds.contains(category.getId())).toList();
  }

  private boolean isPubliclyAvailable(Category category) {
    return category.isActive()
        && category.isPublicVisible()
        && category.getRiskLevel() != CategoryRiskLevel.PROHIBITED
        && category.getLaunchPhase() == CategoryLaunchPhase.MVP;
  }

  private boolean supportsServiceMode(Category category, CategoryServiceMode serviceMode) {
    if (serviceMode == null) {
      return true;
    }
    return switch (serviceMode) {
      case REMOTE -> category.isAllowsRemote();
      case ONSITE -> category.isAllowsOnsite();
      case HYBRID -> category.isAllowsRemote() && category.isAllowsOnsite();
    };
  }

  private List<Category> filterByServiceMode(
      List<Category> categories, CategoryServiceMode serviceMode) {
    if (serviceMode == null) {
      return categories;
    }
    Map<Long, Category> byId =
        categories.stream().collect(Collectors.toMap(Category::getId, category -> category));
    Set<Long> visibleIds =
        categories.stream()
            .filter(category -> supportsServiceMode(category, serviceMode))
            .map(Category::getId)
            .collect(Collectors.toSet());
    for (Long categoryId : List.copyOf(visibleIds)) {
      Category current = byId.get(categoryId);
      while (current != null && current.getParentId() != null) {
        Category parent = byId.get(current.getParentId());
        if (parent == null || !visibleIds.add(parent.getId())) {
          break;
        }
        current = parent;
      }
    }
    return categories.stream().filter(category -> visibleIds.contains(category.getId())).toList();
  }

  private record LocalizedCategoryText(String name) {}
}
