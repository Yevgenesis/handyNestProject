package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.model.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TaskMapper {

    Task toTask(TaskRequestDto taskRequestDto);
    TaskResponseDto toTaskResponseDto(Task task);
    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);


}
