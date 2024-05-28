package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserRequestUpdateDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.exception.UserAlreadyExistsException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.exception.WrongConfirmationPasswordException;
import codezilla.handynestproject.mapper.UserMapper;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> userResponseDtos = userMapper.usersToListDto(users);
        return userResponseDtos;
    }

    @Override
    public UserResponseDto findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        UserResponseDto userResponseDto = userMapper.userToDto(user.orElseThrow(UserNotFoundException::new));
        return userResponseDto;
    }

    @Override
    public User findByIdReturnUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserResponseDto update(UserRequestUpdateDto updateDto) {

        User user = userRepository.findById(updateDto.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(updateDto.firstName());
        user.setLastName(updateDto.lastName());
        user.setLogo(updateDto.logo());

        return userMapper.userToDto(userRepository.save(user));

    }

    @Override
    public void updateRating(User user) {
        Double newRating = userRepository.getRatingByUserId(user.getId());
        user.setPositiveFeedbackPercent(newRating);
        userRepository.save(user);
    }

    @Override
    public void increaseTaskCounterUp(User user) {
        user.increaseTaskCounter();
        userRepository.save(user);
    }

    @Override
    public UserResponseDto create(UserRequestDto dto) {
        if (!dto.isPasswordsMatch()) throw new WrongConfirmationPasswordException();

        User user = userMapper.dtoToUser(dto);
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        UserResponseDto userResponseDto = userMapper.userToDto(user);
        return userResponseDto;
    }
}


