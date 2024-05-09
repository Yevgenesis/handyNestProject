package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;

import java.util.List;

public interface PerformerService {

    PerformerResponseDto createPerformer(PerformerRequestDto performerDto);

    PerformerResponseDto getPerformerById(Long id);

    List<PerformerResponseDto> getPerformers();

}
