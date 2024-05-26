package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.WorkingTime;
import codezilla.handynestproject.repository.WorkingTimeRepository;
import codezilla.handynestproject.service.WorkingTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkingTimeServiceImpl implements WorkingTimeService {

    private WorkingTimeRepository workingTimeRepository;

    @Override
    public WorkingTime findWorkingTimeById(Long id) {
        WorkingTime workingTime = workingTimeRepository.findWorkingTimeById(id);
        return workingTime;
    }
}
