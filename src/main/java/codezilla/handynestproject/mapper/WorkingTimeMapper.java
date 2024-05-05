package codezilla.handynestproject.mapper;

import codezilla.handynestproject.model.entity.WorkingTime;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkingTimeMapper {


    default WorkingTime workingTimeFromId(Long workingTimeId) {
        WorkingTime workingTime = new WorkingTime();
        workingTime.setId(workingTimeId);
        return workingTime;
    }


}
