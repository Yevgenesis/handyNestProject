package codezilla.handynestproject.controller;

import codezilla.handynestproject.HandyNestProjectApplication;
import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.TestData;
import codezilla.handynestproject.util.TestDatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class CategoryControllerTest {


    private final CategoryService categoryService;


    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    CategoryControllerTest(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Test
    void getAllCategoriesTest() throws Exception {

       mockMvc.perform(get("/catigories"))
               .andExpect(status().isOk());




    }
}