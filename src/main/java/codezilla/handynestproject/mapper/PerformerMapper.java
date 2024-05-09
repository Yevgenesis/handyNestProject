package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.model.entity.Performer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, CategoryMapper.class})
public interface PerformerMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    PerformerResponseDto performerToDto(Performer performer);

    List<PerformerResponseDto> performersToListDto(List<Performer> performers);

    @Mapping(target = "user", ignore = true)
        // Если user не нужно устанавливать, исключаем его из маппинга
    Performer dtoToPerformer(PerformerRequestDto performerDto);
}
