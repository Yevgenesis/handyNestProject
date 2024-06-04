package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@Table(name = "address")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entity representing a address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Address id", example = "1")
    private Long id;

    @Schema(description = "Street", example = "Derebasovskay street")
    private String street;

    @Schema(description = "City", example = "New-York")
    private String city;

    @Schema(description = "Zip", example = "12345")
    private String zip;

    @Schema(description = "Country", example = "USA")
    private String country;
}
