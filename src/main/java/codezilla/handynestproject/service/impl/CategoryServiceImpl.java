package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.CategoryResponseDto;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getListCategoryResponseDto() {
        List<Category> rootCategories = categoryRepository.findAll();
        return rootCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryResponseDto mapToDTO(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setWeight(category.getWeight());
        dto.setChildren(getChildrenDTO(category.getId()));
        return dto;
    }

    private List<CategoryResponseDto> getChildrenDTO(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        return children.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
