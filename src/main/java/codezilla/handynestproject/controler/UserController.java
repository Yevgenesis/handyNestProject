package codezilla.handynestproject.controler;

import codezilla.handynestproject.dto.UserResponseDto;
import codezilla.handynestproject.mapper.UserMapper;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;


    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userMapper.usersToListDto(userService.getUsers());
    }

}
