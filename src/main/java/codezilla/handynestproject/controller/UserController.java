package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.user.UserRequestDto;
import codezilla.handynestproject.dto.user.UserRequestUpdateDto;
import codezilla.handynestproject.dto.user.UserResponseDto;
import codezilla.handynestproject.security.service.AuthenticationService;
import codezilla.handynestproject.security.model.JwtAuthenticationResponse;
import codezilla.handynestproject.security.model.SignInRequest;
import codezilla.handynestproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Operations related to users")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Get all users.
     *
     * @return list of all users
     */
    @Operation(summary = "Find all users",
            description = "Return all users",
            security = @SecurityRequirement(name = "users"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<UserResponseDto> findAll() {
        log.info("All users");
        return userService.findAll();
    }

    /**
     * Get a user by ID.
     *
     * @param id the ID of the user
     * @return the user with the specified ID
     */
    @Operation(summary = "Find user by id",
            description = "Return user by id",
            security = @SecurityRequirement(name = "users"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found - User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        log.info("Find user by id: {}", id);
        return userService.findById(id);
    }

    /**
     * Create a new user.
     *
     * @param userRequestDto the user request DTO
     * @return the created user
     */
    @Operation(summary = "Create a new user",
            description = "Return created user",
            security = @SecurityRequirement(name = "users"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created"),
            @ApiResponse(responseCode = "400", description = "User with this email already exists")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public UserResponseDto save(@RequestBody @Valid UserRequestDto userRequestDto) {
        log.info("Created new user: {}", userRequestDto);
        return userService.create(userRequestDto);
    }


    /**
     * Update a user.
     *
     * @param updateDto the user update request DTO
     * @return the updated user
     */
    @Operation(summary = "Update a user",
            description = "Return user with updated data",
            security = @SecurityRequirement(name = "users"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated"),
            @ApiResponse(responseCode = "404", description = "Not found - User not found")
    })
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping
    public UserResponseDto update(@RequestBody @Valid UserRequestUpdateDto updateDto) {
        log.info("Update user: {}", updateDto);
        return userService.update(updateDto);
    }


}

