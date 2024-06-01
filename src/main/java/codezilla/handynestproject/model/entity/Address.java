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
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Address id", example = "1", required = true)
    private Long id;
    @Schema(name = "Street",example = "Derebasovskay street", required = true)
    private String street;
    @Schema(name = "City",example = "New-York", required = true)
    private String city;
    @Schema(name = "Zip",example = "12345", required = true)
    private String zip;
    @Schema(name = "Country",example = "USA", required = true)
    private String country;

}
