package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.category.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getListCategoryResponseDto();
}
