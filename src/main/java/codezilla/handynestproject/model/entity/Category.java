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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Category id",example = "1", required = true)
    private Long id;

    @Schema(name = "Category title",example = "Ремонт", required = true)
    private String title;

    @Schema(name = "Parent category id",example = "1", required = true)
    @Column(name = "parent_id")
    private Long parentId;
    @Schema(name = "Weight category",example = "1", required = true)
    private int weight;


}
