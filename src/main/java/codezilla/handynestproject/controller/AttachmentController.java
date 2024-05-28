package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    //POST
    @PostMapping
    public AttachmentDto create(@RequestBody @Valid AttachmentRequestDto attachmentRequestDto) {
        return attachmentService.create(attachmentRequestDto);
    }

    //GET
    @GetMapping
    public List<AttachmentDto> findAll() {
        return attachmentService.findAll();
    }

    @GetMapping("/{id}")
    public AttachmentDto findById(@PathVariable Long id) {
        return attachmentService.findById(id);
    }


    //PUT
    @PutMapping("performer/{performerId}")
    public List<AttachmentDto> findByPerformerId(@PathVariable Long performerId) {
        return attachmentService.findByPerformerId(performerId);
    }

    // DELETE

    @DeleteMapping
    public void delete(@PathVariable Long attachmentId) {
        attachmentService.remove(attachmentId);
    }

}
