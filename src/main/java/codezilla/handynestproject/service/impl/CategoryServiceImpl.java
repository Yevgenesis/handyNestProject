package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private List<Category> rootCategories;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getListCategoryResponseDto() {
        rootCategories = categoryRepository.findAll();
        return rootCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Category> findCategoriesByIdIn(List<Long> ids) {
        Set<Category> categories = new HashSet<>();
        for (Long id : ids) {
            categories.add(categoryRepository.findById(id)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found")));
        }
        return categories;
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    }

    private CategoryResponseDto mapToDTO(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        dto.setWeight(category.getWeight());
        dto.setChildren(getChildrenDTO(category.getId()));
        return dto;
    }

    private List<CategoryResponseDto> getChildrenDTO(Long parentId) {
        List<Category> children = rootCategories.stream()
                .filter(category -> category.getParentId() != null && category.getParentId().equals(parentId))
                .toList();
        return children.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
