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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the UserService interface.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Retrieves all users.
     *
     * @return a list of UserResponseDto representing all users
     */
    @Override
    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> userResponseDtos = userMapper.usersToListDto(users);
        return userResponseDtos;
    }

    /**
     * Finds a user by its ID and returns a UserResponseDto.
     *
     * @param id the ID of the user to find
     * @return a UserResponseDto representing the user
     * @throws UserNotFoundException when user not  found
     */
    @Override
    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        //TODO add User isDeleted, проверки по ролям. Если запрос от админа, то возврат всех.
        //   Если юзер, то себя. Перформер только не удаленных
        return userMapper.userToDto(user);
    }


    /**
     * Finds a user by its ID and returns the User entity.
     *
     * @param id the ID of the user to find
     * @return the User entity
     * @throws UserNotFoundException when user not found
     */
    @Override
    public User findByIdReturnUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    /**
     * Checks if a user exists by its ID.
     *
     * @param id the ID of the user to check
     * @return true if the user exists, false otherwise
     */
    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Updates a user with the given data.
     *
     * @param updateDto the data to update the user with
     * @return a UserResponseDto representing the updated user
     * @throws UserNotFoundException when user not found
     */
    @Override
    public UserResponseDto update(UserRequestUpdateDto updateDto) {

        User user = userRepository.findById(updateDto.id())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(updateDto.firstName());
        user.setLastName(updateDto.lastName());
        user.setLogo(updateDto.logo());

        return userMapper.userToDto(userRepository.save(user));

    }

    /**
     * Logs whether a user with the given ID exists.
     *
     * @param id the ID of the user to check
     */
    @Override
    public void checkExists(Long id) {
        UserResponseDto user = findById(id);
        log.debug("User {} with id {} exists", user, id);
    }

    /**
     * Updates the rating of the given user.
     *
     * @param user the user whose rating to update
     */
    @Override
    public void updateRating(User user) {
        Double newRating = userRepository.getRatingByUserId(user.getId());
        user.setPositiveFeedbackPercent(newRating);
        userRepository.save(user);
    }

    /**
     * Increases the task counter for the given user.
     *
     * @param user the user whose task counter to increase
     */
    @Override
    public void increaseTaskCounterUp(User user) {
        user.increaseTaskCounter();
        userRepository.save(user);
    }

    /**
     * Creates a new user with the given data.
     *
     * @param dto the data to create the user with
     * @return a UserResponseDto representing the created user
     * @throws UserAlreadyExistsException when user with this email already exist
     */
    @Override
    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        if (!dto.isPasswordsMatch()) throw new WrongConfirmationPasswordException();

        // Password encoding
        User user = userMapper.dtoToUser(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        UserResponseDto userResponseDto = userMapper.userToDto(user);
        return userResponseDto;
    }

    /**
     * Retrieves a user by its email.
     *
     * @param email the email of the user to find
     * @return the User entity with the given email
     * @throws UserNotFoundException if user with given email not found
     */
    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User with email " + email + " not found"));
    }

    /**
     * Retrieves the current user's ID from the security context.
     *
     * @return the ID of the current user
     * @throws SecurityException        if the user is not authenticated
     * @throws IllegalArgumentException if the primary authentication object cannot be used to obtain the ID
     */
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


