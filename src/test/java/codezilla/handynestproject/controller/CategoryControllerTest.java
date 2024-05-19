package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.util.TestDatabaseConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class CategoryControllerTest {


    private final CategoryService categoryService;

    @Autowired
    CategoryControllerTest(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Test
    void getAllCategoriesTest() throws Exception {
       List<CategoryResponseDto> actual = categoryService.getListCategoryResponseDto();
        Assertions.assertEquals(47, actual.size());



    }
}