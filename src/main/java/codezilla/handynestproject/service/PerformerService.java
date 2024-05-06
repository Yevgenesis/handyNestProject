package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.performer.PerformerResponseDto;

import java.util.List;

public interface PerformerService {

    PerformerResponseDto getPerformerById(Long id);

    List<PerformerResponseDto> getPerformers();

}
