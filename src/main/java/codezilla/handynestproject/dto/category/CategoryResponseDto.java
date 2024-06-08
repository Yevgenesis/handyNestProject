package codezilla.handynestproject.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    @Schema(description = "Category ID")
    private Long id;
    @Schema(description = "Category title", example = "Music")
    private String title;
    @Schema(description = "List of child categories")
    private List<CategoryResponseDto> children;
    @Schema(description = "Category weight")
    private int weight;
}
