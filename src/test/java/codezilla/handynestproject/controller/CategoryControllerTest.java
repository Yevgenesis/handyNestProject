package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static codezilla.handynestproject.service.TestData.CATEGORY_RESPONSE_DTO;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories() throws Exception {

        List<CategoryResponseDto> expected = List.of(CATEGORY_RESPONSE_DTO);
        when(categoryService.getListCategoryResponseDto()).thenReturn(expected);
        List<CategoryResponseDto> actual = categoryService.getListCategoryResponseDto();

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        assertEquals("Lists should be equal", expected, actual);

    }
}