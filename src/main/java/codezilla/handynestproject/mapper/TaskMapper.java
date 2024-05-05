package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.dto.task.TaskWithPerformerResponseDto;
import codezilla.handynestproject.model.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring", uses = {WorkingTimeMapper.class, CategoryMapper.class, PerformerMapper.class})

public interface TaskMapper {

    Task toTask(TaskRequestDto taskRequestDto);
    Task toTaskUpdate(TaskUpdateRequestDto taskUpdateRequestDto);

    @Mapping(target = "user.email", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.created_on", ignore = true)
    @Mapping(target = "user.updated_on", ignore = true)
    @Mapping(target = "user.tasks", ignore = true)
    @Mapping(target = "user.roles", ignore = true)
    @Mapping(target = "user.sentFeedbacks", ignore = true)
    @Mapping(target = "user.receivedFeedbacks", ignore = true)


    TaskResponseDto toTaskResponseDto(Task task);

    TaskWithPerformerResponseDto toTaskWithPerformerResponseDto(Task task);
    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);


}
