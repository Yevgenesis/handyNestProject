package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getUsers();

    UserResponseDto getUserById(Long id);

    void updateRating(User user);

    void increaseTaskCounterUp(User user);
}
