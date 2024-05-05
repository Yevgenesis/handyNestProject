package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto userToDto(User user);
    List<UserResponseDto> usersToListDto(List<User> users);



}
