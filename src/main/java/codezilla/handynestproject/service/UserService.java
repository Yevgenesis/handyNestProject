package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getUsers();

    UserResponseDto getUserById(Long id);
}
