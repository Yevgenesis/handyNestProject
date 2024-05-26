package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.attachment.AttachmentDto;

import java.util.List;

public interface AttachmentService {

    AttachmentDto create(AttachmentRequestDto dto);

    AttachmentDto findById(Long attachmentId);

    List<AttachmentDto> findAll();

    List<AttachmentDto> findByPerformerId(Long performerId);



    void remove(Long attachmentId);

}
