package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.WorkingTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the WorkingTimeService interface.
 */

@Service
@RequiredArgsConstructor
public class WorkingTimeServiceImpl implements WorkingTimeService {

    private final WorkingTimeRepository workingTimeRepository;

    /**
     * Finds a WorkingTime entity by its ID.
     *
     * @param id the ID of the WorkingTime entity to find
     * @return the WorkingTime entity with the given ID
     */

    @Override
    public WorkingTime findWorkingTimeById(Long id) {
        WorkingTime workingTime = workingTimeRepository.findWorkingTimeById(id);
        return workingTime;
    }
}
