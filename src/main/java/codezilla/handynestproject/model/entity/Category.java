package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
@Schema(description = "Entity representing a category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Category id", example = "1")
    private Long id;

    @Schema(description = "Category title", example = "Ремонт")
    private String title;

    @Schema(description = "Parent category id", example = "1")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "Weight category", example = "1")
    private int weight;
}
