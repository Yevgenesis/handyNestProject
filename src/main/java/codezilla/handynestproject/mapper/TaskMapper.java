package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.model.entity.Task;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring", uses = {UserMapper.class, WorkingTimeMapper.class, CategoryMapper.class, PerformerMapper.class})

public interface TaskMapper {


    TaskResponseDto toTaskResponseDto(Task task);

    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);


}
