package codezilla.handynestproject.mapper;

import codezilla.handynestproject.dto.task.TaskRequestDto;
import codezilla.handynestproject.dto.task.TaskResponseDto;
import codezilla.handynestproject.dto.task.TaskUpdateRequestDto;
import codezilla.handynestproject.model.entity.Task;
import codezilla.handynestproject.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;


@Mapper(componentModel = "spring", uses = {WorkingTimeMapper.class, CategoryMapper.class})

public interface TaskMapper {

    Task toTask(TaskRequestDto taskRequestDto);
    Task toTaskUpdate(TaskUpdateRequestDto taskUpdateRequestDto);

    @Mapping(target = "user.firstName", source = "user.firstName", conditionQualifiedByName = "mapUserFirstName")
    @Mapping(target = "user.lastName", source = "user.lastName", conditionQualifiedByName = "mapUserLastName")
    @Mapping(target = "user.email", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.created_on", ignore = true)
    @Mapping(target = "user.updated_on", ignore = true)
    @Mapping(target = "user.tasks", ignore = true)
    @Mapping(target = "user.roles", ignore = true)
    @Mapping(target = "user.sentFeedbacks", ignore = true)
    @Mapping(target = "user.receivedFeedbacks", ignore = true)
//    @Mapping(target = "user.isDeleted", ignore = true)
//    @Mapping(target = "user.isEmailVerified", ignore = true)

    TaskResponseDto toTaskResponseDto(Task task);
    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);


    @Named("mapUserFirstName")
    default String mapUserFirstName(User user) {
        return user.getFirstName();
    }
    @Named("mapUserLastName")
    default String mapUserLastName(User user) {
        return user.getLastName();
    }

}
