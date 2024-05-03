package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.Performer;

import java.util.List;
import java.util.Optional;

public interface PerformerService {
    List<Performer> getPerformers();

    Optional<Performer> getPerformerById(Long id);
}
