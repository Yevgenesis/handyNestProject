package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.model.entity.Performer;

import java.util.List;

public interface PerformerService {

    PerformerResponseDto create(PerformerRequestDto performerDto);

    PerformerResponseDto findById(Long id);

    List<PerformerResponseDto> findAll();

    PerformerResponseDto update(PerformerUpdateRequestDto performerDto);


    void updateRating(Performer performer);

    void increaseTaskCounterUp(Performer performer);

    PerformerResponseDto updateAvailability(Long id, boolean isPublish);
}
