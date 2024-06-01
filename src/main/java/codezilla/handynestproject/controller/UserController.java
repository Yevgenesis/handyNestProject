package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserRequestUpdateDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.security.AuthenticationService;
import codezilla.handynestproject.security.model.JwtAuthenticationResponse;
import codezilla.handynestproject.security.model.SignInRequest;
import codezilla.handynestproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

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

    @PostMapping("/login")
    public JwtAuthenticationResponse login(@RequestBody SignInRequest request) {
        return authenticationService.authenticate(request);
    }
    // PUT
    @PutMapping
    public UserResponseDto update(@RequestBody @Valid UserRequestUpdateDto updateDto) {
        return userService.update(updateDto);
    }


}

