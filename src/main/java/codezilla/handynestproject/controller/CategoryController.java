package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
@Tag(name = "Category Controller", description = "Operations related to categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Retrieves all categories.
     *
     * @return a list of CategoryResponseDto representing all categories
     */
    @Operation(summary = "Find all categories", description = "Return all categories",
            security = @SecurityRequirement(name = "categories"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Categories not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return categoryService.getListCategoryResponseDto();
    }

}
