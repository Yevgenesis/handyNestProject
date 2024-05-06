package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.model.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryTitleDto categoryToTitleDto(Category category);

    List<CategoryTitleDto> categoryToListTitleDto(List<Category> categories);

    CategoryResponseDto categoryToDto(Category category);

    List<CategoryResponseDto> categoryToListDto(List<Category> categories);
}
