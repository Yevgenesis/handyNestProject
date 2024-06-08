package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring", uses = {AddressMapper.class, UserMapper.class, CategoryMapper.class, PerformerMapper.class})

public interface TaskMapper {


    Task toTask(TaskResponseDto taskResponseDto);    Task taskUpdatetoDtoTask(TaskUpdateRequestDto taskResponseDto);

    Task toTaskUpdate(TaskUpdateRequestDto taskUpdateRequestDto);

    TaskResponseDto toTaskResponseDto(Task task);

    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);
}
