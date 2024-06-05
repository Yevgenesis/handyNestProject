package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping(path = "/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Controller", description = "Operations related to attachment")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Creates a new attachment.
     *
     * @param attachmentRequestDto the attachment request DTO
     * @return the created attachment
     */
    @Operation(summary = "Create new attachment", description = "Return new attachment", security = @SecurityRequirement(name = "attachments"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created"),
            @ApiResponse(responseCode = "404", description = "Attachment not created")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PostMapping
    public AttachmentDto create(@RequestBody @Valid AttachmentRequestDto attachmentRequestDto) {
        log.info("Create Attachment Request: {}", attachmentRequestDto);
        return attachmentService.create(attachmentRequestDto);
    }

    /**
     * Retrieves all attachments.
     *
     * @return a list of all attachments
     */
    @Operation(summary = "Find all attachments", description = "Return all attachments", security = @SecurityRequirement(name = "attachments"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<AttachmentDto> findAll() {
        log.info("Find All Attachments");
        return attachmentService.findAll();
    }

    /**
     * Retrieves an attachment by its ID.
     *
     * @param id the ID of the attachment
     * @return the attachment with the given ID
     */
    @Operation(summary = "Find attachment by id", description = "Return attachment", security = @SecurityRequirement(name = "attachments"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public AttachmentDto findById(@PathVariable Long id) {
        log.info("Find Attachment by id: {}", id);
        return attachmentService.findById(id);
    }

    /**
     * Retrieves attachments by performer ID.
     *
     * @param performerId the ID of the performer
     * @return a list of attachments associated with the performer
     */
    @Operation(summary = "Find attachment by performer id", description = "Return attachment by performer id", security = @SecurityRequirement(name = "attachments"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("performer/{performerId}")
    public List<AttachmentDto> findByPerformerId(@PathVariable Long performerId) {
        log.info("Find Attachment by performerId: {}", performerId);
        return attachmentService.findByPerformerId(performerId);
    }

    /**
     * Deletes an attachment by its ID.
     *
     * @param attachmentId the ID of the attachment to delete
     */
    @Operation(summary = "Remove attachment by id", description = "Attachment is deleted", security = @SecurityRequirement(name = "attachments"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{attachmentId}")
    public void delete(@PathVariable Long attachmentId) {
        log.info("Delete Attachment by id: {}", attachmentId);
        attachmentService.remove(attachmentId);
    }
}