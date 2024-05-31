package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.exception.AttachmentNotFoundException;
import codezilla.handynestproject.mapper.AttachmentMapper;
import codezilla.handynestproject.model.entity.Attachment;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.repository.AttachmentRepository;
import codezilla.handynestproject.service.AttachmentService;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;
    private final PerformerService performerService;

    @Override
    public AttachmentDto create(AttachmentRequestDto dto) {
        Performer performer = performerService.findByIdReturnPerformer(dto.getPerformerId()); // проверка на перформера
        Attachment attachment = Attachment.builder()
                .performer(performer)
                .fileName(dto.getFileName())
                .type(dto.getType())
                .url(dto.getUrl())
                .build();
        Attachment savedAttachment = attachmentRepository.save(attachment);
        AttachmentDto attachmentDto = attachmentMapper.attachmentToDto(savedAttachment);
        return attachmentDto;
    }

    @Override
    public AttachmentDto findById(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(AttachmentNotFoundException::new);
        return attachmentMapper.attachmentToDto(attachment);
    }

    @Override
    public List<AttachmentDto> findAll() {
        return attachmentMapper.attachmentsToListDto(attachmentRepository.findAll());
    }

    @Override
    public List<AttachmentDto> findByPerformerId(Long performerId) {
        return attachmentMapper.attachmentsToListDto(attachmentRepository.findAllByPerformerId(performerId));
    }

    @Override
    public void remove(Long attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }
}
