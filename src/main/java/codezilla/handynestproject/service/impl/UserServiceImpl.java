package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.UserMapper;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {// для секьюрити: implements UserDetailsService

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public List<UserResponseDto> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> userResponseDtos = userMapper.usersToListDto(users);
        return userResponseDtos;
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        UserResponseDto userResponseDto = userMapper.userToDto(user.orElseThrow(UserNotFoundException::new));
        return userResponseDto;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return userRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Can't find user by username: " + username));// ToDo fix exception
//    }
}
