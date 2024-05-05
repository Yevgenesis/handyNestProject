package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PerformerServiceImpl implements PerformerService {

    private final PerformerRepository performerRepository;


    @Override
    public List<Performer> getPerformers() {
        return performerRepository.findAll();
    }

    @Override
    public Optional<Performer> getPerformerById(Long id) {
        Performer performer = performerRepository.findPerformerById(id);
        return Optional.ofNullable(performer);
    }

}
