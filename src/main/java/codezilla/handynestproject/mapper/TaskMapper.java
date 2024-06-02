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

//    @Mapping(target = "address.street", source = "street")
//    @Mapping(target = "address.city", source = "city")
//    @Mapping(target = "address.zip", source = "zip")
//    @Mapping(target = "address.country", source = "country")
    Task toTask(TaskRequestDto taskRequestDto);

    Task toTaskUpdate(TaskUpdateRequestDto taskUpdateRequestDto);

    TaskResponseDto toTaskResponseDto(Task task);

    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);
}
