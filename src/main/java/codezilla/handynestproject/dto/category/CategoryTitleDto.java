package codezilla.handynestproject.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTitleDto {
    @Schema(description = "Category ID")
    private Long id;
    @Schema(description = "Category title", example = "Music")
    private String title;
}

