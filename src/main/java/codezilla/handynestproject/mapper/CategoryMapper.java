package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.CategoryResponseDto;
import codezilla.handynestproject.model.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDto categoryToDto(Category category);

    List<CategoryResponseDto> categoryToListDto(List<Category> categories);
}
