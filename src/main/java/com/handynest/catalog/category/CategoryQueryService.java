package com.handynest.catalog.category;

import codezilla.handynestproject.model.entity.Category;
import com.handynest.common.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryQueryRepository categoryRepository;
    private final CategoryTranslationRepository translationRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findTree(String locale) {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAscIdAsc();
        return toTree(categories, loadLocalizedTexts(categories, locale));
    }

    @Transactional(readOnly = true)
    public CategoryResponse findByPublicId(String publicId, String locale) {
        Category category = categoryRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", publicId));
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, List<Category>> childrenByParentId = groupChildren(categories);

        return toResponse(category, childrenByParentId, loadLocalizedTexts(categories, locale));
    }

    private List<CategoryResponse> toTree(
            List<Category> categories,
            Map<Long, LocalizedCategoryText> localizedTexts
    ) {
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
            Map<Long, LocalizedCategoryText> localizedTexts
    ) {
        List<CategoryResponse> children = childrenByParentId
                .getOrDefault(category.getId(), List.of())
                .stream()
                .sorted(categoryComparator())
                .map(child -> toResponse(child, childrenByParentId, localizedTexts))
                .toList();
        LocalizedCategoryText text = localizedTexts.getOrDefault(
                category.getId(),
                new LocalizedCategoryText(category.getTitle())
        );

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
                category.getWeight(),
                children
        );
    }

    private Comparator<Category> categoryComparator() {
        return Comparator.comparingInt(Category::getSortOrder)
                .thenComparing(Category::getId);
    }

    private Map<Long, LocalizedCategoryText> loadLocalizedTexts(List<Category> categories, String locale) {
        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();
        List<CategoryTranslation> translations = translationRepository.findByCategoryIdInAndLocaleIn(
                categoryIds,
                List.of(locale, "ru")
        );

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
        categories.forEach(category -> localizedTexts.putIfAbsent(
                category.getId(),
                new LocalizedCategoryText(category.getTitle())
        ));

        return localizedTexts;
    }

    private record LocalizedCategoryText(String name) {
    }
}
