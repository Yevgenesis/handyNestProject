//package codezilla.handynestproject.controller;
//
//import codezilla.handynestproject.model.entity.Role;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Set;
//
//@RestController
//@RequestMapping(name = "/roles")
//@RequiredArgsConstructor
//public class RoleController {
//    private final RoleService roleService;
//
//    @GetMapping
//    public Set<Role> getRolesByUser(@RequestParam(name = "userId", required = true) Long userId) {
//        return roleService.getRolesByUserId(userId);
//    }
//}
