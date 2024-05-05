package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.model.entity.Performer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PerformerMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "performer.created_on", target = "createdOn")
    @Mapping(source = "performer.updated_on", target = "updatedOn")
    PerformerResponseDto performerToDto(Performer performer);

    List<PerformerResponseDto> performersToListDto(List<Performer> performers);
}
