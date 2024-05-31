package codezilla.handynestproject.security;

import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.model.enums.RoleName;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getByEmail(username);


        var res =  new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
//                List.of(new SimpleGrantedAuthority("USER"))); // подтянуть все роли юзера
                user.getRoles().stream().map(auth -> new SimpleGrantedAuthority(auth.name()))
                        .collect(Collectors.toList()));
        return res;
    }
}
