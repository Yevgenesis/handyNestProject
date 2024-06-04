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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
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
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.userToDto(user);
    }
//TODO add User isDeleted
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
    public void checkExists(Long id) {
        UserResponseDto user = findById(id);
        log.debug("User {} with id {} exists", user, id);
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
    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        if (!dto.isPasswordsMatch()) throw new WrongConfirmationPasswordException();



        User user = userMapper.dtoToUser(dto);

        String encodedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encodedPassword);
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        UserResponseDto userResponseDto = userMapper.userToDto(user);
        return userResponseDto;
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User with email " + email + " not found"));
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            User user = getByEmail(email);
            return user.getId();
        } else {
            throw new IllegalArgumentException("The primary authentication object cannot be used to obtain the ID");
        }
    }
}


