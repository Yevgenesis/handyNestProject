package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public UserResponseDto save(@RequestBody @Valid UserRequestDto userRequestDto) {
        return userService.create(userRequestDto);
    }


}

