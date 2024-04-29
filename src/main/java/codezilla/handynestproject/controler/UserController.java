package codezilla.handynestproject.controler;

import codezilla.handynestproject.model.entity.UserInfo;
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

    public final UserService userService;

    @GetMapping
    public List<UserInfo> getAllUsers() {
        return userService.getUsers();
    }

//    @PostMapping
//    public UserInfo addUser(@RequestBody UserInfo userInfo) {
//
//    }
}
