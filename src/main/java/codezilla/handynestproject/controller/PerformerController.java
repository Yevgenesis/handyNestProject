package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.service.PerformerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/performers")
@RequiredArgsConstructor
@Tag(name = "Performer Controller", description = "Operations related to performer")
public class PerformerController {

    private final PerformerService performerService;

    /**
     * Creates a new performer.
     *
     * @param performerDto the performer request DTO
     * @return the created performer response DTO
     */
    @Operation(summary = "Create a new performer", description = "Return created performer",
            security = @SecurityRequirement(name = "performers"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public PerformerResponseDto create(@RequestBody @Valid PerformerRequestDto performerDto) {
        log.info("Performer request: {}", performerDto);
        return performerService.create(performerDto);
    }

    /**
     * Finds all performers.
     *
     * @return the list of all performers
     */
    @Operation(summary = "Find all performers", description = "Return all performers",
            security = @SecurityRequirement(name = "performers"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<PerformerResponseDto> findAll() {
        log.info("Performer request");
        return performerService.findAll();
    }

    /**
     * Finds a performer by ID.
     *
     * @param id the ID of the performer
     * @return the performer response DTO
     */
    @Operation(summary = "Find performer by id", description = "Return performer by id",
            security = @SecurityRequirement(name = "performers"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "User with this email already exists")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public PerformerResponseDto findById(@PathVariable Long id) {
        log.info("Find performer by id: {}", id);
        return performerService.findById(id);
    }


    /**
     * Updates a performer.
     *
     * @param updateDto the performer update request DTO
     * @return the performer with updated data
     */
    @Operation(summary = "Update a performer", description = "Performer data have been updated",
            security = @SecurityRequirement(name = "performers"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "User with this email already exists")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping
    public PerformerResponseDto update(@RequestBody @Valid PerformerUpdateRequestDto updateDto) {
        log.info("Update performer: {}", updateDto);
        return performerService.update(updateDto);
    }

    /**
     * Updates the availability of a performer by ID.
     *
     * @param id the ID of the performer
     * @param isAvailable the availability status
     * @return the performer with updated availability
     */
    @Operation(summary = "Update availability by performer id",
            description = "Return performer with updated availability",
            security = @SecurityRequirement(name = "performers"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "User with this email already exists")
    })
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @PutMapping("/{id}/{isAvailable}")
    public PerformerResponseDto updateAvailability(@PathVariable Long id, @PathVariable Boolean isAvailable) {
        log.info("Update availability: {}", isAvailable);
        return performerService.updateAvailability(id, isAvailable);
    }

}
