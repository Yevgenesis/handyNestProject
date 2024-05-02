package codezilla.handynestproject.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResponseDto {

    private Long id;
    private String name;
    private List<CategoryResponseDto> children;
    private int weight;

}
