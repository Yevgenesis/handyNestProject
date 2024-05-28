package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.model.entity.Category;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    List<CategoryResponseDto> getListCategoryResponseDto();

    Set<Category> findCategoriesByIdIn(List<Long> ids);

    Category findById(Long id);
}
