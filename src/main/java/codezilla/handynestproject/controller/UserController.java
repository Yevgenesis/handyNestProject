package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET
    @GetMapping
    public List<UserResponseDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST
    @PostMapping
    public UserResponseDto save(@RequestBody UserRequestDto userRequestDto) {
        return userService.create(userRequestDto);
    }


}

