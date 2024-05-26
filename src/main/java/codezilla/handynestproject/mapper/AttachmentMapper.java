package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.attachment.AttachmentDto;
import codezilla.handynestproject.dto.attachment.AttachmentRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.model.entity.Attachment;
import codezilla.handynestproject.model.entity.Performer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(source = "performer.id", target = "performerId")
    AttachmentDto attachmentToDto(Attachment attachment);

    Attachment dtoToAttachment(AttachmentRequestDto attachmentDto);


    List<AttachmentDto> attachmentsToListDto(List<Attachment> attachments);


    List<Attachment> dtoToListAttachment(List<AttachmentDto> attachmentDtos);
}
