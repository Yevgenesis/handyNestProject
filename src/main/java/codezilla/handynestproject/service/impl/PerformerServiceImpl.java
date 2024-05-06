package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PerformerServiceImpl implements PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;


    @Override
    @Transactional
    public List<PerformerResponseDto> getPerformers() {
        List<Performer> performers = performerRepository.findAll();
        List<PerformerResponseDto> dtos = performerMapper.performersToListDto(performers);
        return dtos;
    }

    @Override
    public PerformerResponseDto getPerformerById(Long id) {
        Optional<Performer> performer = performerRepository.findById(id);
        PerformerResponseDto dtos = performerMapper.performerToDto(performer.get()); // ToDo exception
        return dtos;
    }

}
