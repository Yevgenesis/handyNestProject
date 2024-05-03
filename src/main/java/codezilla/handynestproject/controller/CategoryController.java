package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.getListCategoryResponseDto();
    }

}
