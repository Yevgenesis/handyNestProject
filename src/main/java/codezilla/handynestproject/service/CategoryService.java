package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getListCategoryResponseDto();
}
