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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    // GET
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<UserResponseDto> findAll() {
        return userService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    // POST
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public UserResponseDto save(@RequestBody @Valid UserRequestDto userRequestDto) {
        return userService.create(userRequestDto);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping("/login")
    public JwtAuthenticationResponse login(@RequestBody SignInRequest request) {
        return authenticationService.authenticate(request);
    }
    // PUT
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping
    public UserResponseDto update(@RequestBody @Valid UserRequestUpdateDto updateDto) {
        return userService.update(updateDto);
    }


}

