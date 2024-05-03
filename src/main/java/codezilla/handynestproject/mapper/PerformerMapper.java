package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PerformerMapper {

    PerformerResponseDto performerToDto(User user);

    List<PerformerResponseDto> performersToListDto(List<Performer> performers);
}
