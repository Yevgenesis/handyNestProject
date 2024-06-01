package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.attachment.AttachmentDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping(path = "/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Controller", description = "Operations related to attachment")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * post request
     * @param attachmentRequestDto
     * @return new attachment
     */
    @Operation(summary = "Create new attachment",
            description = "Return new attachment",
            security = @SecurityRequirement(name = "swagger-ui"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Attachment not created")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PostMapping
    public AttachmentDto create(@RequestBody @Valid AttachmentRequestDto attachmentRequestDto) {
        log.info("Create Attachment Request: {}", attachmentRequestDto);
        return attachmentService.create(attachmentRequestDto);
    }

    /**
     * get request
     * @return all attachments
     */
    @Operation(summary = "Find all attachments",
            description = "Return all attachments",
            security = @SecurityRequirement(name = "swagger-ui"))
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
     * get request
     * @param id
     * @return attachment by id
     */
    @Operation(summary = "Find attachment by id",
            description = "Return attachment",
            security = @SecurityRequirement(name = "swagger-ui"))
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
     * put request
     * @param performerId
     * @return attachment by performer id
     */
    @Operation(summary = "Find attachment by performer id",
            description = "Return attachment by performer id",
            security = @SecurityRequirement(name = "swagger-ui"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("performer/{performerId}")
    public List<AttachmentDto> findByPerformerId(@PathVariable Long performerId) {
        log.info("Find Attachment by performerId: {}", performerId);
        return attachmentService.findByPerformerId(performerId);
    }

    /**
     * delete request
     * @param attachmentId
     */
    @Operation(summary = "Remove attachment by id",
            description = "Return attachment is deleted",
            security = @SecurityRequirement(name = "swagger-ui"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Performer not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping
    public void delete(@PathVariable Long attachmentId) {
        log.info("Delete Attachment by id: {}", attachmentId);
        attachmentService.remove(attachmentId);
    }

}
