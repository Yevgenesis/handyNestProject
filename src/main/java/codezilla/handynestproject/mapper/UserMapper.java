package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.user.UserNestedResponseDto;
import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto userToDto(User user);

    User toUser(UserResponseDto userResponseDto);

    User dtoToUser(UserRequestDto userRequestDto);

    List<UserResponseDto> usersToListDto(List<User> users);

    UserNestedResponseDto userToNestedDto(User user);

    List<UserNestedResponseDto> usersToListNestedDto(List<User> users);


}
