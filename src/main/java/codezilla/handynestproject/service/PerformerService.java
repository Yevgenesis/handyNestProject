package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.model.entity.Performer;

import java.util.List;

public interface PerformerService {

    PerformerResponseDto createPerformer(PerformerRequestDto performerDto);

    PerformerResponseDto getPerformerById(Long id);

    List<PerformerResponseDto> getPerformers();

    PerformerResponseDto updatePerformer(PerformerRequestDto performerDto);


    void updateRating(Performer performer);

    void increaseTaskCounterUp(Performer performer);

    PerformerResponseDto updateAvailability(Long id, boolean isPublish);
}
