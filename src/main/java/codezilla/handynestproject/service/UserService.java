package codezilla.handynestproject.service;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserRequestUpdateDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.model.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponseDto> findAll();

    UserResponseDto findById(Long id);


    void updateRating(User user);

    void increaseTaskCounterUp(User user);

    UserResponseDto create(UserRequestDto userRequestDto);

    User getByEmail(String email);

    Long getCurrentUserId();

    User findByIdReturnUser(Long id);

    boolean existsById(Long id);

    UserResponseDto update(UserRequestUpdateDto updateDto);

    void checkExists(Long id);
}
