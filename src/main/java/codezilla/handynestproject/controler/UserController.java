package codezilla.handynestproject.controler;

import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/user")
public class UserController {

    public final UserService userService;


    @GetMapping
    public List<User> getAllUsers() {
        return userService.getUsers();
    }
}
