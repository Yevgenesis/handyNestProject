package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    //POST
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PostMapping
    public AttachmentDto create(@RequestBody @Valid AttachmentRequestDto attachmentRequestDto) {
        return attachmentService.create(attachmentRequestDto);
    }

    //GET
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<AttachmentDto> findAll() {
        return attachmentService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public AttachmentDto findById(@PathVariable Long id) {
        return attachmentService.findById(id);
    }


    //PUT
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("performer/{performerId}")
    public List<AttachmentDto> findByPerformerId(@PathVariable Long performerId) {
        return attachmentService.findByPerformerId(performerId);
    }

    // DELETE
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping
    public void delete(@PathVariable Long attachmentId) {
        attachmentService.remove(attachmentId);
    }

}
